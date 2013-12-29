(defproject picflic "0.1.0-SNAPSHOT"
  :description "Picture importer"
  :url "http://bripkens.de"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main picflic.importer
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.7.8"]
                 [cheshire "5.3.0"]])
