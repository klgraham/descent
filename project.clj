(defproject descent "0.1.0-SNAPSHOT"
  :description "Track and visualize project dependencies over time, with Clojure, Datomic, and Rhizome."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [rhizome "0.2.0"]
                 [midje "1.6.2"]]

  :main ^:skip-aot descent.core
  :plugins [[codox "0.6.7"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
