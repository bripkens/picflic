(defproject api "0.1.0-SNAPSHOT"
  :description "Flickr for your home!"
  :url "http://bripkens.de"
  :dependencies [[org.clojure/clojure "1.5.1"]

                 ; web & routing
                 [compojure "1.1.6"]
                 [liberator "0.10.0"]

                 ; JSON handling
                 [com.github.fge/json-schema-validator "2.1.7"]
                 [cheshire "5.3.0"]

                 ; persistence
                 [com.novemberain/monger "1.5.0"]

                 ; logging
                 [org.clojure/tools.logging "0.2.6"]
                 [log4j/log4j "1.2.17"]

                 ; miscealanous
                 [org.clojure/math.numeric-tower "0.0.2"]
                 [clj-time "0.6.0"]
                 [com.google.guava/guava "15.0"]
                 [com.mortennobel/java-image-scaling "0.8.5"]]
  :plugins [[lein-ring "0.8.8"]]
  :ring {:handler api.handler/handler}
  :jvm-opts ["-Xmx1g" "-server"]
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}})
