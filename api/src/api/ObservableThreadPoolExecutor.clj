(ns api.ObservableThreadPoolExecutor
  (:gen-class
    :extends java.util.concurrent.ThreadPoolExecutor
    :main false
    :init init
    :constructors {[int
                    int
                    long
                    java.util.concurrent.TimeUnit]
                   [int
                    int
                    long
                    java.util.concurrent.TimeUnit
                    java.util.concurrent.BlockingQueue]}
    :state state)
  (:require [clojure.java.jmx :as jmx])
  (:import [java.util.concurrent TimeUnit LinkedBlockingQueue]))

(defn -init [core-pool-size maximum-pool-size keep-alive-time unit]
  (let [state (ref {:queued 0 :failed 0 :succeeded 0})]
    (jmx/register-mbean (jmx/create-bean state)
                    "api.concurrency:name=queue-stats")
    [[core-pool-size
      maximum-pool-size
      keep-alive-time
      unit
      (LinkedBlockingQueue.)]
     state]))

(defn -beforeExecute [this thread runnable]
  (dosync (alter (.state this) #(assoc % :queued (inc (:queued %))))))

(defn -afterExecute [this _ throwable]
  (dosync
    (alter
      (.state this)
      (fn [state]
        (let [tmp-state (assoc state :queued (dec (:queued state)))
              key-to-inc (if (nil? throwable) :succeeded :failed)]
          (assoc tmp-state key-to-inc (inc (key-to-inc tmp-state))))))))
