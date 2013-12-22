(ns api.handler
  (:require [cheshire.core :refer [generate-string parse-string]]
            [compojure.core :refer [defroutes ANY]]
            [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [api.mongo :as mongo]))

(def config (edn/read-string (slurp "config.edn")))


(defresource configuration
  :available-media-types ["application/json"]
  :handle-ok config)


(defresource collection-list
  :available-media-types ["application/json"]
  :handle-ok (mongo/get-collections))


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
      (wrap-params)))
