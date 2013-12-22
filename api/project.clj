(defproject api "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [liberator "0.10.0"]
                 [cheshire "5.3.0"]
                 [com.novemberain/monger "1.5.0"]]
  :plugins [[lein-ring "0.8.8"]]
  :ring {:handler api.handler/handler}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
