(ns picflic.mongo
  (:require [monger.core :as mg]
            [monger.collection :as mgc])
  (:import [org.bson.types ObjectId]))

(mg/connect!)

(mg/set-db! (mg/get-db "monger-test"))

(defn save-collection [collection]
  (mgc/insert "collections" (assoc collection :_id (ObjectId.))))
