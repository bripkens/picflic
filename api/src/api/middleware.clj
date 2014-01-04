(ns api.middleware
  (:require [clojure.tools.logging :as logging]))

(defn wrap-cors
  "Allow requests from all origins"
  [handler]
  (fn [request]
    (let [response (handler request)]
      (update-in response
                 [:headers "Access-Control-Allow-Origin"]
                 (fn [_] "*")))))


(defn wrap-logging
  "Log all requests"
  [handler]
  (fn [request]
    (logging/trace "Request:"
                   (.toUpperCase (.substring (str (:request-method request)) 1))
                   (:uri request))
    (handler request)))
