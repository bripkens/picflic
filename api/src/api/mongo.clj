(ns api.mongo
  (:require [monger.core :as mg]
            [monger.collection :as mgc])
  (:import [org.bson.types ObjectId]))

(mg/connect!)

(mg/set-db! (mg/get-db "monger-test"))

(defn add-collection [collection]
  (mgc/insert "collections" (assoc collection :_id (ObjectId.))))

(defn get-collections []
  (map #(dissoc % :_id) (mgc/find-maps "collections")))

(defn get-collection [id]
  (dissoc (mgc/find-one-as-map "collections" {:id id}) :_id))

(defn get-image [id]
  (first (filter #(= (:id %) id)
                 (:images (mgc/find-one-as-map "collections"
                                               {"images.id" id}
                                               ["images"])))))
