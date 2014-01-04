(ns api.concurrency
  (:require [api.config :refer [config]])
  (:import [java.util.concurrent Executors]))

(def agent-executor (Executors/newFixedThreadPool
                      (:image-resizing-threads config)))
