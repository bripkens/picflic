(ns api.handler
  (:require [cheshire.core :refer [generate-string parse-string]]
            [compojure.core :refer [defroutes ANY]]
            [liberator.core :refer [resource defresource]]
            [liberator.dev :refer [wrap-trace]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [api.mongo :as mongo]))

(def config (edn/read-string (slurp "config.edn")))


;; convert the body to a reader. Useful for testing in the repl
;; where setting the body to a string is much simpler.
(defn body-as-string [ctx]
  (if-let [body (get-in ctx [:request :body])]
    (condp instance? body
      java.lang.String body
      (slurp (io/reader body)))))


;; For PUT and POST parse the body as json and store in the context
;; under the given key.
(defn parse-json [context key]
  (when (#{:put :post} (get-in context [:request :request-method]))
    (try
      (if-let [body (body-as-string context)]
        (let [data (parse-string body)]
          [false {key data}])
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


(defresource configuration
  :available-media-types ["application/json"]
  :handle-ok config)


(defresource collection-list
  :allowed-methods [:post :get]
  :available-media-types ["application/json"]
  :handle-ok (mongo/get-collections)
  :known-content-type? #(check-content-type % ["application/json"])
  :malformed? #(parse-json % ::data)
  :post! (fn [ctx]
           (let [collection (::data ctx)]
             (mongo/add-collection collection))))


(defresource collection-entries [id]
  :available-media-types ["application/json"]
  :exists? (fn [_]
             (let [e (mongo/get-collection id)]
               (if-not (nil? e)
                 {::entry e})))
  :handle-ok ::entry)


(defn- construct-image-path [image]
  (str (:image-directory config)
       "/"
       (:name image)
       ".jpg"))


(defresource image-entries [id]
  :available-media-types ["image/jpeg"]
  :exists? (fn [_]
             (let [e (mongo/get-image id)]
               (if-not (nil? e)
                 {::entry e})))
  :handle-ok (fn [ctx] (io/file (construct-image-path (::entry ctx)))))


(defroutes app
  (ANY "/config" [] configuration)
  (ANY "/collections" [] collection-list)
  (ANY "/collections/:id" [id] (collection-entries id))
  (ANY "/images/:id" [id] (image-entries id)))


(def handler
  (-> app
      (wrap-params)
      (wrap-trace ::header :ui)))
