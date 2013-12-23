(ns api.handler
  (:require [cheshire.core :refer [generate-string parse-string]]
            [compojure.core :refer [defroutes ANY]]
            [compojure.route :as route]
            [liberator.core :refer [resource defresource]]
            [liberator.dev :refer [wrap-trace]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [api.mongo :as mongo]
            [api.request-validation :refer [parse-request check-content-type]]))

(def config (edn/read-string (slurp "config.edn")))


(defresource configuration
  :available-media-types ["application/json"]
  :handle-ok config)


(defresource collection-list
  :allowed-methods [:post :get]
  :available-media-types ["application/json"]
  :handle-ok (mongo/get-collections)
  :known-content-type? #(check-content-type % ["application/json"])
  :malformed? #(parse-request % "collection")
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
  (ANY "/images/:id" [id] (image-entries id))
  (route/resources "/"))


(def handler
  (-> app
      (wrap-params)
      (wrap-trace ::header :ui)))
