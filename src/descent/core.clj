(ns descent.core
  (:require [descent.pom-parser :as parser]
            [descent.dependency-graph :as g])
  (:use [clojure.pprint :only (pprint)])
  (:gen-class))

(defn -main
  "Given a pom.xml path, this will extract the project's dependencies, load
  them into Datomic (so they're viewable over time), generate a graph
  visualization and then print the graph to a file.

  Usage: descent.core: <path-to-pom> <output-file> [optional arguments]"
  [path-to-pom output-file & args]
  (let [deps-map (parser/process-pom path-to-pom)
        graph (g/create-graph deps-map)
        image (g/create-image graph)
        graph-name (str (:project-name deps-map) "-" (:project-version deps-map))]
    (pprint deps-map)
    (g/save-image-to-file image output-file)
    (g/save-graph-to-file graph (str graph-name ".graph"))))
