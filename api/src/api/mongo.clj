(ns api.mongo
  (:require [monger.core :as mg]
            [monger.collection :as mgc]
            [clj-time.core :as joda]
            monger.joda-time)
  (:import [org.bson.types ObjectId]
           org.joda.time.DateTimeZone))


;; set default time zone that a org.joda.time.DateTime instances
;; will use
(DateTimeZone/setDefault DateTimeZone/UTC)


(mg/connect!)

(mg/set-db! (mg/get-db "monger-test"))


(defn add-collection [collection]
  (let [id (ObjectId.)
        transformed-collection (-> collection
                                   (assoc :_id id)
                                   (assoc :images [])
                                   (assoc :modified (joda/now)))]
    (mgc/insert "collections" transformed-collection)
    (str id)))


(defn get-collection [id]
  (mgc/find-map-by-id "collections" (ObjectId. id)))


(defn get-collections []
  (mgc/find-maps "collections"))


(defn get-next-image-id [] (str (ObjectId.)))


(defn save-image [collection-id image-id image-data]
  (let [image (assoc image-data
                :_id (ObjectId. image-id)
                :modified (joda/now))
        collection (get-collection collection-id)
        new-collection (assoc collection
                         :images
                         (conj (:images collection) image))]
    (mgc/update-by-id "collections" (:_id collection) new-collection)
    image))


(defn get-image [collection-id image-id]
  (first (filter #(= (:_id %) (ObjectId. image-id))
                 (:images (get-collection collection-id)))))
