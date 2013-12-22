(ns picflic.image-loader
  (:require [clojure.java.io :as io]
            [clojure.math.numeric-tower :as math])
  (:import (javax.imageio ImageIO)
           (com.mortennobel.imagescaling ResampleOp)))

(def image-types ["png" "jpeg" "jpg" "bmp"])

(def supported-screen-sizes [{:name "WQHD"
                              :width 2560
                              :height 1440}
                             {:name "FHD"
                              :width 1920
                              :height 1080}
                             {:name "HD"
                              :width 1366
                              :height 768}
                             {:name "WXGA"
                              :width 1280
                              :height 800}
                             {:name "XGA"
                              :width 1024
                              :height 768}])

(defn- is-image?
  "Verifies whether the given java.io.File is a readable image"
  [f]
  (let [name (.getName f)]
    (and (.exists f)
      (not (.isHidden f))
      (.isFile f)
      (.canRead f)
      (some #(.endsWith (.toLowerCase name) (.toLowerCase %))
        image-types))))


(defn- annotate
  "Gather meta information about the image and returns a map with all the
  necessary information for later processing."
  [f]
  (let [buffered-image (ImageIO/read f)
        width (.getWidth buffered-image)
        height (.getHeight buffered-image)]
    {:name (second (re-matches #"^(.*)\.[a-zA-Z]+$" (.getName f)))
     :path (.getAbsolutePath f)
     :width width
     :height height
     :aspect-ratio (/ width height)}))


(defn get-images
  "Returns a lazy sequence of maps where each map represents an image"
  [path]
  (let [directory (io/file path)]
    (assert (.exists directory))
    (assert (.isDirectory directory))
    (assert (.canRead directory))
    (map annotate (filter is-image? (file-seq directory)))))


(defn- get-scaled-resolution
  "Calculates a new target resolution that has the width of the given
   resolution, and the height in accordance to the image's aspect raio."
  [image resolution]
  (assoc resolution
    :height
    (int (math/round (* (/ 1 (:aspect-ratio image)) (:width resolution))))))


(defn get-target-resolutions
  [image]
  (reduce
    (fn [target-resolutions resolution]
      (if (<= (:width resolution) (:width image))
          (conj target-resolutions (get-scaled-resolution image resolution))
          target-resolutions))
    []
    supported-screen-sizes))


(defn- scale-image
  [image writer]
  (map (fn [resolution]
         (let [f (io/file (:path image))
               buffered-image (ImageIO/read f)
               operation (new ResampleOp
                              (:width resolution)
                              (:height resolution))
               scaled-image (.filter operation buffered-image nil)]
           (writer image resolution scaled-image)
           resolution))
       (get-target-resolutions image)))


(defn scale-images
  [images writer]
  (map (fn [image]
         (assoc image :resolutions (scale-image image writer)))
       images))
