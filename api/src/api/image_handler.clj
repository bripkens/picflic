(ns api.image-handler
  (:require [api.config :refer [config]]
            [api.concurrency :refer [agent-executor]]
            [clojure.java.io :as io]
            [clojure.math.numeric-tower :as math]
            [clojure.tools.logging :as logging]
            [clj-time.coerce :as time-coerce])
  (:import (javax.imageio ImageIO IIOException)
           (com.drew.imaging ImageMetadataReader)
           (com.drew.metadata.exif ExifSubIFDDirectory)
           (com.mortennobel.imagescaling ResampleOp)))


; Create the image directory if it does not exist
(let [dir (:image-directory config)
      file (io/file dir)]
  (if (not (.exists file))
    (when-not (.mkdirs file)
      (throw (RuntimeException. "Could not create image output directory.")))))


(defn image-path
  ([id]
   (format "%s/%s.jpg" (:image-directory config) id))
  ([id resolution]
   (format "%s/%s_%sx%s.jpg"
           (:image-directory config)
           id
           (:width resolution)
           (:height resolution))))


(defn get-resolution [image width]
  (let [resolutions (sort-by #(% :width) (image :resolutions))]
    (if-let [larger (some #(if (>= (% :width) width) %) resolutions)]
      larger)))


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
    (:image-target-resolutions config)))


(defn- read-time-taken [file]
  (if-let [metadata (ImageMetadataReader/readMetadata file)]
    (if-let [exif (.getDirectory metadata ExifSubIFDDirectory)]
      (if-let [date (.getDate exif ExifSubIFDDirectory/TAG_DATETIME_ORIGINAL)]
        (time-coerce/from-date date)))))


(defn analyze [image-id]
  (try
    (let [file (io/file (image-path image-id))
          buffered-image (ImageIO/read file)
          width (.getWidth buffered-image)
          height (.getHeight buffered-image)
          resolutions (get-target-resolutions width (/ width height))
          time-taken (read-time-taken file)]
      {:width width
       :height height
       :resolutions resolutions
       :time-taken time-taken})
    (catch IIOException e
      (logging/warn "Attempt to upload image failed with" e)
      nil)))


(defn- scale [image {width :width height :height :as resolution}]
  (let [start (System/currentTimeMillis)
        buffered-image (ImageIO/read (io/file (image-path (:_id image))))
        operation (ResampleOp. width height)
        scaled-image (.filter operation buffered-image nil)
        output (io/file (image-path (:_id image) resolution))]
    (ImageIO/write scaled-image "jpg" output)
    (logging/debug "Successfully scaled image" (str (:_id image))
                   "to" width "x" height "in"
                   (- (System/currentTimeMillis) start) "ms")))


(defn scale-async [image]
  (doseq [resolution (:resolutions image)]
    (let [a (agent false)]
      (send-via agent-executor a (fn [_] (scale image resolution))))))
