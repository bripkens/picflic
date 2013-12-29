(ns api.image-handler
  (:require [api.util :refer [config]]
            [api.concurrency :refer [agent-executor]]
            [clojure.java.io :as io]
            [clojure.math.numeric-tower :as math]
            [clojure.tools.logging :as logging])
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


(defn- get-target-resolutions [width aspect-ratio]
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
        resolutions (get-target-resolutions width (/ width height))]
    {:width width
     :height height
     :resolutions resolutions}))


(defn- scale [image {width :width height :height :as resolution}]
  (logging/debug "Scaling image" (str (:_id image)) "to" width "x" height)
  (let [buffered-image (ImageIO/read (io/file (image-path (:_id image))))
        operation (ResampleOp. width height)
        scaled-image (.filter operation buffered-image nil)
        output (io/file (image-path (:_id image) resolution))]
    (ImageIO/write scaled-image "jpg" output)
    (logging/debug "Successfully scaled image"
                   (str (:_id image))
                   "to"
                   width
                   "x"
                   height)))


(defn scale-async [image]
  (doseq [resolution (:resolutions image)]
    (let [a (agent false)]
      (send-via agent-executor a (fn [_] (scale image resolution))))))
