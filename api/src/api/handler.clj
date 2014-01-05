(ns api.handler
  (:require monger.json
            [compojure.core :refer [defroutes ANY GET]]
            [compojure.route :as route]
            [liberator.core :refer [resource defresource]]
            [liberator.dev :refer [wrap-trace]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :as response]
            [clojure.java.io :as io]
            [clojure.tools.logging :as logging]
            [api.mongo :as mongo]
            [api.middleware :as middleware]
            [api.config :refer [config]]
            [api.image-handler :as images]
            [api.util :as util]
            [api.request-validation :refer [parse-request check-content-type]]))


(defresource configuration
  :available-media-types ["application/json"]
  :handle-ok config)


(defn- sort-images [collection]
  (assoc collection :images (sort-by #(:time-taken %) (:images collection))))


(defresource collection-list
  :allowed-methods [:post :get]
  :available-media-types ["application/json"]
  :handle-ok (fn [_] (map sort-images (mongo/get-collections)))
  :known-content-type? #(check-content-type % ["application/json"])
  :malformed? #(parse-request % "collection_command" ::data)
  :post! (fn [ctx]
           (let [collection (::data ctx)
                 id (mongo/add-collection collection)]
             {::id id}))
  :handle-created #(mongo/get-collection (::id %))
  :location #(util/build-entry-url (get % :request) (get % ::id)))


(defresource collection-entries [id]
  :available-media-types ["application/json"]
  :exists? (fn [_]
             (let [e (mongo/get-collection id)]
               (if-not (nil? e)
                 {::entry e})))
  :handle-ok #(sort-images (::entry %)))


(defresource image-list [collection-id]
  :allowed-methods [:post]
  :available-media-types ["application/json"]
  :malformed? [false]
  :known-content-type? #(check-content-type % ["image/jpeg"])
  :post! (fn [{{body :body} :request}]
           (let [id (mongo/get-next-image-id)
                 file (io/file (images/image-path id))]
             (with-open [out (io/output-stream file)] (io/copy body out))
             ; TODO handle error cases:
             ; Image could not be analyzed (e,g. IIOException), or
             ; it is of wrong type, or
             ; it is too large
             (if-let [analyzed (images/analyze id)]
               (let [result (mongo/save-image collection-id
                                              id
                                              analyzed)]
                 (images/scale-async result)
                 {::id id}))))
  :handle-created #(mongo/get-image collection-id (::id %))
  :location #(util/build-entry-url (get % :request) (get % ::id)))


(defresource image-entries [collection-id image-id]
  :allowed-methods [:get :post]
  :available-media-types ["image/jpeg" "application/json"]
  :exists? (fn [_]
             (let [e (mongo/get-image collection-id image-id)]
               (if-not (nil? e)
                 {::entry e})))
  :handle-ok (fn [{image ::entry {media-type :media-type} :representation
                   request :request}]
               (case media-type
                 "image/jpeg"
                   (if-let [width (get-in request [:query-params "width"])]
                     (if-let [resolution (images/get-resolution image (Integer/parseInt width))]
                       (io/file (images/image-path (:_id image) resolution))
                       (io/file (images/image-path (:_id image))))
                     (io/file (images/image-path (:_id image))))
                 "application/json"
                   image)))


(defroutes app
  (ANY "/config" [] configuration)
  (ANY "/collections" [] collection-list)
  (ANY "/collections/:id" [id] (collection-entries id))
  (ANY "/collections/:collection-id/images" [collection-id]
       (image-list collection-id))
  (ANY "/collections/:collection-id/images/:image-id" [collection-id image-id]
       (image-entries collection-id image-id))
  (GET "/" [] (response/redirect "/api/index.html"))
  (route/resources "/"))


(def handler
  (-> app
      (wrap-params)
      (middleware/wrap-cors)
      (middleware/wrap-logging)))
