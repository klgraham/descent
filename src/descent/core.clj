(ns descent.core
  (:require [descent.pom-parser :as parser])
  (:use [clojure.pprint :only (pprint)])
  (:gen-class))

(defn -main
  "Given a pom.xml path, this will extract the project's dependencies, load
  them into Datomic (so they're viewable over time), generate a graph
  visualization and then print the graph to a file.

  Usage: descent.core: <path-to-pom> [optional arguments]"
  [path-to-pom & args]
  (let [deps-map (parser/process-pom path-to-pom)]
    (pprint deps-map)))
