(ns api.concurrency
  (:require [api.util :refer [config]])
  (:import [java.util.concurrent Executors]))

(def agent-executor (Executors/newFixedThreadPool
                      (:image-resizing-threads config)))
