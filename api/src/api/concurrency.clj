(ns api.concurrency
  (:require [api.config :refer [config]])
  (:import [java.util.concurrent Executors]))

(def
  ^{:doc "The thread pool which should be used for Clojure's agents"}
  agent-executor (Executors/newFixedThreadPool
                      (:image-resizing-threads config)))
