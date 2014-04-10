(defproject descent "1.0"
  :description "Track and visualize project dependencies over time, with Clojure, Datomic, and Rhizome."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [rhizome "0.2.0"]
                 [midje "1.6.3"]]

  :main ^:skip-aot descent.core
  :plugins [[codox "0.6.7"]
            [lein-midje "3.1.1"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[midje "1.5.1"]]}})
