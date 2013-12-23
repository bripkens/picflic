(ns api.request-validation
  (:require [cheshire.core :refer [parse-string]]
            [clojure.java.io :as io])
  (:import [com.fasterxml.jackson.databind ObjectMapper]
           [com.github.fge.jsonschema.main JsonSchemaFactory]
           [com.github.fge.jsonschema.load.configuration LoadingConfiguration]
           [com.github.fge.jsonschema.load.uri URITransformer]))


;; convert the body to a reader. Useful for testing in the repl
;; where setting the body to a string is much simpler.
; (defn- body-as-string [ctx]
;   (if-let [body (get-in ctx [:request :body])]
;     (condp instance? body
;       java.lang.String body
;       (slurp (io/reader body)))))


;; For PUT and POST parse the body as json and store in the context
;; under the given key.
; (defn- parse-json [context key]
;   (when (#{:put :post} (get-in context [:request :request-method]))
;     (try
;       (if-let [body (body-as-string context)]
;         (let [data (parse-string body)]
;           [false {key data}])
;         {:message "No body"})
;       (catch Exception e
;         (.printStackTrace e)
;         {:message (format "IOException: " (.getMessage e))}))))

; (import com.github.fge.jsonschema.main JsonSchemaFactory)
; (import '[com.fasterxml.jackson.databind ObjectMapper])
; (def jsonSchema (.getJsonSchema (JsonSchemaFactory/byDefault) "file:///Users/ben/projects/picflic/api/resources/public/api/schema/collection.json"))
; (def json (-> (ObjectMapper.) .reader (.readTree "{\"name\":\"foo\"}")))

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

