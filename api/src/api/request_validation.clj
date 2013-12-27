(ns api.request-validation
  (:require [cheshire.core :refer [parse-string]]
            [clojure.java.io :as io])
  (:import [com.fasterxml.jackson.databind ObjectMapper]
           [com.github.fge.jsonschema.main JsonSchemaFactory]
           [com.github.fge.jsonschema.load.configuration LoadingConfiguration]
           [com.github.fge.jsonschema.load.uri URITransformer]))


(def
  ^{:private true
    :doc "An immutable and therefore thread-safe JSON schema factory.
         You can call (.getJsonSchema json-schema-factory <json-schema-node>)
         to retrieve a JsonSchema instance which can validate JSON.
         This is a constant as the construction is expensive."}
  json-schema-factory
  (let [transformer (-> (URITransformer/newBuilder)
                        (.setNamespace "resource:/public/api/schema/")
                        .freeze)
        loading-config (-> (LoadingConfiguration/newBuilder)
                           (.setURITransformer transformer)
                           .freeze)
        factory (-> (JsonSchemaFactory/newBuilder)
                    (.setLoadingConfiguration loading-config)
                    .freeze)]
    factory))


(def
  ^{:private true
    :doc "Initialize the object mapper first and keep it private as not all
         of its methods are thread-safe. Optionally configure it here.
         Reader instances are cheap to create."}
  object-reader
  (let [object-mapper (ObjectMapper.)]
    (fn [] (.reader object-mapper))))


(defn- parse-to-node
  "Parse the given String as JSON. Returns a Jackson JsonNode."
  [data] (-> (object-reader) (.readTree data)))


(defn- get-schema
  "Get the schema file's contents in form of a string. Function only expects
  the schema name, i.e. 'collection' or 'image'."
  [schema-name]
  (slurp (io/resource (str "public/api/schema/" schema-name ".json"))))


(defn validate
  "Validates the given 'data' against the JSON schema. Returns an object
  with an :success property that equals true when the schema could
  successfully be validated. It additionally contains a :message property
  with a human readable error description."
  [schema-name data]
  (let [parsed-schema (parse-to-node (get-schema schema-name))
        schema (-> json-schema-factory (.getJsonSchema parsed-schema))
        parsed-data (parse-to-node data)
        report (.validate schema parsed-data)]
    {:success (.isSuccess report)
     :message (str report)}))


;; convert the body to a reader. Useful for testing in the repl
;; where setting the body to a string is much simpler.
(defn- body-as-string [ctx]
  (if-let [body (get-in ctx [:request :body])]
    (condp instance? body
      java.lang.String body
      (slurp (io/reader body)))))


(defn parse-request [ctx schema-name]
  (when (#{:put :post} (get-in ctx [:request :request-method]))
    (try
      (if-let [body (body-as-string ctx)]
        (let [validation-report (validate schema-name body)]
          (if (:success validation-report)
            [false {::data (parse-string body)}]
            {:message (:message validation-report)}))
        {:message "No body"})
      (catch Exception e
        (.printStackTrace e)
        {:message (format "IOException: " (.getMessage e))}))))


;; For PUT and POST check if the content type is json.
(defn check-content-type [ctx content-types]
  (if (#{:put :post} (get-in ctx [:request :request-method]))
    (or
     (some #{(get-in ctx [:request :headers "content-type"])}
           content-types)
     [false {:message "Unsupported Content-Type"}])
    true))
