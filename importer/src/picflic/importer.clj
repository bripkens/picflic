(ns picflic.importer
  (:gen-class)
  (:require [clojure.edn :as edn]
            [picflic.image-loader :as loader]
            [picflic.mongo :as mongo])
  (:import (javax.imageio ImageIO)))

; (deftest gets-images
;   (let [directory "/Users/ben/projects/picflic/resources/dummy-pics"
;         images (get-images directory)]
;     (doseq [finished-image (scale-images images (fn [a b c] 1))]
;       (save-image finished-image))))

(defn- read-config []
  (edn/read-string (slurp "config.edn")))

(defn- mkdir [new-dir] (.mkdir (java.io.File. new-dir)))

(defn- image-storage [output]
  (fn [image resolution scaled-image]
    (let [target (format "%s/%s-%dx%d.jpg"
                         output
                         (:name image)
                         (:width resolution)
                         (:height resolution))]
      (println "Storing image: " target)
      (ImageIO/write scaled-image "jpg" (java.io.File. target)))))

(defn -main []
  (let [config (read-config)
        input (:input config)
        output (:output config)
        raw-images (loader/get-images input)]
    (mkdir output)
    (doseq [finished-image (loader/scale-images raw-images (image-storage output))]
      (mongo/save-image finished-image))))
