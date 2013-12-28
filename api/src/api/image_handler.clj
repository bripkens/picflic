(ns api.image-handler
  (:require [api.util :refer [config]]
            [clojure.java.io :as io]
            [clojure.math.numeric-tower :as math])
  (:import (javax.imageio ImageIO)
           (com.mortennobel.imagescaling ResampleOp)))

(def
  ^{:private true}
  supported-screen-sizes
  [{:width 2560
    :height 1440}
   {:width 1920
    :height 1080}
   {:width 1366
    :height 768}
   {:width 1280
    :height 800}
   {:width 1024
    :height 768}])


(defn image-path
  ([id]
   (format "%s/%s.jpg" (:image-directory config) id))
  ([id resolution]
   (format "%s/%s_%sx%s.jpg"
           (:image-directory config)
           id
           (:width resolution)
           (:height resolution))))


(defn get-target-resolutions [width aspect-ratio]
  (reduce
    (fn [target-resolutions resolution]
      (if (<= (:width resolution) width)
          (let [target-width (:width resolution)
                target-height (int (math/round (* (/ 1 aspect-ratio)
                                                  target-width)))]
            (conj target-resolutions {:width target-width
                                      :height target-height}))
          target-resolutions))
    []
    supported-screen-sizes))


(defn analyze [image-id]
  (let [file (io/file (image-path image-id))
        buffered-image (ImageIO/read file)
        width (.getWidth buffered-image)
        height (.getHeight buffered-image)
        aspect-ratio (/ width height)
        resolutions (get-target-resolutions width aspect-ratio)]
    {:width width
     :height height
     :aspect-ratio aspect-ratio
     :resolutions resolutions}))


(defn- scale [image resolution]
  (println "Going to scale image...")
  (let [buffered-image (ImageIO/read (io/file (image-path (:_id image))))
        operation (ResampleOp. (:width resolution) (:height resolution))
        scaled-image (.filter operation buffered-image nil)
        output (io/file (image-path (:_id image) resolution))]
    (ImageIO/write scaled-image "jpg" output)
    (println "Scaled image!")))


(defn scale-async [image]
  (doseq [resolution (:resolutions image)]
    (let [a (agent false)]
      (send a (fn [_] (scale image resolution))))))