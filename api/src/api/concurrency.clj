(ns api.concurrency
  (:require [api.config :refer [config]]
            [clojure.java.jmx :as jmx])
  (:import [api ObservableThreadPoolExecutor]
           [java.util.concurrent TimeUnit]))

(def
  ^{:doc "The thread pool which should be used for Clojure's agents"}
  agent-executor
  (let [exec-config (:image-resizing-executor config)]
    (ObservableThreadPoolExecutor.
      {:core-pool-size (:core-pool-size exec-config)
       :maximum-pool-size (:maximum-pool-size exec-config)
       :keep-alive-time (:keep-alive-time-seconds exec-config)
       :unit TimeUnit/SECONDS
       :jmx-domain "api.concurrency"
       :jmx-bean-name "image-scaling-executor"})))
