(defproject picflic "0.1.0-SNAPSHOT"
  :description "Picture importer"
  :url "http://bripkens.de"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main picflic.importer
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/math.numeric-tower "0.0.2"]
                 [com.novemberain/monger "1.5.0"]
                 [com.mortennobel/java-image-scaling "0.8.5"]])
