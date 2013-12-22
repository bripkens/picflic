(ns picflic.image-loader-test
  (:require [clojure.test :refer :all]
            [picflic.image-loader :refer :all]
            [picflic.mongo :refer :all]))


(defn test-image [width height]
  {:name "Dummy Image"
   :width width
   :height height
   :aspect-ratio (/ width height)})

(defn- contains-resolution? [width height resolutions]
  (some
    #(and
       (= (:width %) width)
       (= (:height %) height))
    resolutions))

(deftest should-find-target-resolutions
  (let [image (test-image 1920 1080)
        resolutions (get-target-resolutions image)]
    (is (contains-resolution? 1920 1080 resolutions))
    (is (contains-resolution? 1366 768 resolutions))
    (is (contains-resolution? 1280 720 resolutions))
    (is (contains-resolution? 1024 576 resolutions))))

(deftest gets-images
  (let [directory "/Users/ben/projects/picflic/resources/dummy-pics"
        images (get-images directory)]
    (doseq [finished-image (scale-images images (fn [a b c] 1))]
      (save-image finished-image))))
