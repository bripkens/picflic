(defproject api "0.1.0-SNAPSHOT"
  :description "Flickr for your home!"
  :url "http://bripkens.de"
  :dependencies [[org.clojure/clojure "1.5.1"]

                 ; Monitoring
                 [org.clojure/java.jmx "0.2.0"]

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

                 ; image handling
                 [com.drewnoakes/metadata-extractor "2.6.2"]
                 [com.mortennobel/java-image-scaling "0.8.5"]

                 ; miscealanous
                 [org.clojure/math.numeric-tower "0.0.2"]
                 [clj-time "0.6.0"]
                 [com.google.guava/guava "15.0"]]
  :plugins [[lein-ring "0.8.8"]]
  :ring {:handler api.handler/handler}
  :jvm-opts ["-Xmx1g"
             "-server"
             "-Dcom.sun.management.jmxremote"
             "-Dcom.sun.management.jmxremote.local.only=false"
             "-Dcom.sun.management.jmxremote.port=7676"
             "-Dcom.sun.management.jmxremote.authenticate=false"
             "-Dcom.sun.management.jmxremote.ssl=false"]
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}})
