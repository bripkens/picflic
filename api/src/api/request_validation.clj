(ns api.request-validation
  (:require [cheshire.core :refer [parse-string]]
            [clojure.java.io :as io])
  (:import [com.fasterxml.jackson.databind ObjectMapper]
           [com.github.fge.jsonschema.main JsonSchemaFactory]
           [com.github.fge.jsonschema.load.configuration LoadingConfiguration]
           [com.github.fge.jsonschema.load.uri URITransformer]))


(def ^:private json-schema-factory
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

(def ^:private object-mapper (ObjectMapper.))

(defn- parse-to-tree [data] (-> object-mapper .reader (.readTree data)))

(defn- get-schema [schema-name]
  (slurp (io/resource (str "public/api/schema/" schema-name ".json"))))


(defn validate [schema-name data]
  (let [parsed-schema (parse-to-tree (get-schema schema-name))
        schema (-> json-schema-factory (.getJsonSchema parsed-schema))
        parsed-data (parse-to-tree data)
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
