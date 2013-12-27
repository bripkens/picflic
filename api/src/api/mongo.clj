(ns api.mongo
  (:require [monger.core :as mg]
            [monger.collection :as mgc])
  (:import [org.bson.types ObjectId]))

(mg/connect!)

(mg/set-db! (mg/get-db "monger-test"))


(defn add-collection [collection]
  (let [id (ObjectId.)]
    (mgc/insert "collections" (assoc collection :_id id))
    (str id)))


(defn get-collection [id]
  (println id)
  (mgc/find-map-by-id "collections" (ObjectId. id)))


(defn get-collections []
  (mgc/find-maps "collections"))

(defn get-image [id]
  (first (filter #(= (:id %) id)
                 (:images (mgc/find-one-as-map "collections"
                                               {"images.id" id}
                                               ["images"])))))
