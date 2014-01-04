(ns api.handler
  (:require monger.json
            [compojure.core :refer [defroutes ANY GET]]
            [compojure.route :as route]
            [liberator.core :refer [resource defresource]]
            [liberator.dev :refer [wrap-trace]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :as response]
            [clojure.java.io :as io]
            [api.mongo :as mongo]
            [api.config :refer [config]]
            [api.image-handler :as images]
            [api.request-validation :refer [parse-request check-content-type]])
  (:import [java.net URL]))


(defn- build-entry-url [request id]
  (URL. (format "%s://%s:%s%s/%s"
                (name (:scheme request))
                (:server-name request)
                (:server-port request)
                (:uri request)
                (str id))))


(defresource configuration
  :available-media-types ["application/json"]
  :handle-ok config)


(defresource collection-list
  :allowed-methods [:post :get]
  :available-media-types ["application/json"]
  :handle-ok (mongo/get-collections)
  :known-content-type? #(check-content-type % ["application/json"])
  :malformed? #(parse-request % "collection_command" ::data)
  :post! (fn [ctx]
           (let [collection (::data ctx)
                 id (mongo/add-collection collection)]
             {::id id}))
  :handle-created #(mongo/get-collection (::id %))
  :location #(build-entry-url (get % :request) (get % ::id)))


(defresource collection-entries [id]
  :available-media-types ["application/json"]
  :exists? (fn [_]
             (let [e (mongo/get-collection id)]
               (if-not (nil? e)
                 {::entry e})))
  :handle-ok ::entry)


(defresource image-list [collection-id]
  :allowed-methods [:post]
  :available-media-types ["application/json"]
  :malformed? [false]
  :known-content-type? #(check-content-type % ["image/jpeg"])
  :post! (fn [{{body :body} :request}]
           (let [id (mongo/get-next-image-id)
                 file (io/file (images/image-path id))]
             (with-open [out (io/output-stream file)]
               (io/copy body out))
             (let [result (mongo/save-image collection-id
                                            id
                                            (images/analyze id))]
               (images/scale-async result)
               {::id id})))
  :handle-created #(mongo/get-image collection-id (::id %))
  :location #(build-entry-url (get % :request) (get % ::id)))


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


(defn wrap-cors
  "Allow requests from all origins"
  [handler]
  (fn [request]
    (let [response (handler request)]
      (update-in response
                 [:headers "Access-Control-Allow-Origin"]
                 (fn [_] "*")))))


(def handler
  (-> app
      (wrap-params)
      (wrap-cors)))
