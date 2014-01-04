(ns api.concurrency
  (:require [api.config :refer [config]]
            [clojure.java.jmx :as jmx])
  (:import [java.util.concurrent Executors]))

(def queue-stats (ref {}))
(defn- update-queue-stats! [size] (dosync (ref-set queue-stats {:size size})))
(update-queue-stats! 0)

(jmx/register-mbean (jmx/create-bean queue-stats)
                    "api.concurrency:name=queue-stats")

(def
  ^{:doc "The thread pool which should be used for Clojure's agents"}
  agent-executor (Executors/newFixedThreadPool
                      (:image-resizing-threads config)))
