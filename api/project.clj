(defproject api "0.1.0-SNAPSHOT"
  :description "Flickr for your home!"
  :url "http://bripkens.de"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [liberator "0.10.0"]
                 [com.github.fge/json-schema-validator "2.1.7"]
                 [cheshire "5.3.0"]
                 [com.novemberain/monger "1.5.0"]
                 [clj-time "0.6.0"]
                 [org.clojure/math.numeric-tower "0.0.2"]
                 [com.mortennobel/java-image-scaling "0.8.5"]]
  :plugins [[lein-ring "0.8.8"]]
  :ring {:handler api.handler/handler}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
