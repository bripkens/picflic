(ns picflic.importer
  (:gen-class)
  (:require [clojure.edn :as edn]
            [cheshire.core :refer [parse-string generate-string]]
            [clojure.java.io :as io]
            [clj-http.client :as client]))

(defn- read-config [] (edn/read-string (slurp "config.edn")))

(def image-types ["jpeg" "jpg"])

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


(defn- get-images
  "Returns a lazy sequence of maps where each map represents an image"
  [path]
  (let [directory (io/file path)]
    (assert (.exists directory))
    (assert (.isDirectory directory))
    (assert (.canRead directory))
    (map #(.getAbsolutePath %) (filter is-image? (file-seq directory)))))


(defn- get-collection-id [collection]
  (let [response (client/get "http://127.0.0.1:3000/collections")
        parsed-body (parse-string (:body response))
        matching-collections (filter #(.equalsIgnoreCase collection (% "name"))
                                     parsed-body)]
  (if (empty? matching-collections)
    (let [post-response (client/post "http://127.0.0.1:3000/collections"
                                     {:headers {"Content-Type" "application/json"}
                                      :body (generate-string
                                              {:name collection})})
          parsed-post-body (parse-string (:body post-response))]
      (parsed-post-body "_id"))
    ((first matching-collections) "_id"))))


(defn- upload [cid image]
  (client/post (format "http://127.0.0.1:3000/collections/%s/images" cid)
               {:body (io/file image)
                :headers {"Content-Type" "image/jpeg"}}))


(defn -main []
  (let [config (read-config)
        input (:input config)
        input-absolute (.getAbsolutePath (io/file input))
        images (get-images input)
        collections (group-by #(-> (io/file %) .getParentFile .getName) images)]
    (doseq [[collection imgs] collections]
      (println "Handling upload of: " collection)
      (let [id (get-collection-id collection)]
        (println "Collection ID is " id)
        (doseq [img imgs]
          (println "Uploading img")
          (upload id img))))))
