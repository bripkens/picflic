(ns picflic.importer
  (:gen-class)
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [picflic.image-loader :as loader]
            [picflic.mongo :as mongo])
  (:import (javax.imageio ImageIO)
           (java.util UUID)))


(defn- read-config [] (edn/read-string (slurp "config.edn")))


(defn- image-storage [output]
  (fn [image resolution scaled-image]
    (let [target (format "%s/%s-%dx%d.jpg"
                         output
                         (:name image)
                         (:width resolution)
                         (:height resolution))]
      (println "Storing image: " target)
      (ImageIO/write scaled-image "jpg" (java.io.File. target)))))

(defn- uuid [] (str (UUID/randomUUID)))

(defn- copy-original [output image]
  (let [target (format "%s/%s.jpg" output (:name image))]
    (io/copy (io/file (:path image)) (io/file target))))

(defn -main []
  (let [config (read-config)
        input (:input config)
        output (:output config)
        raw-images (loader/get-images input)
        finished-images (loader/scale-images raw-images (image-storage output))]
    (doseq [img raw-images] (copy-original output img))
    (mongo/save-collection {:id (uuid)
                            :images (map #(dissoc % :path) finished-images)})))
