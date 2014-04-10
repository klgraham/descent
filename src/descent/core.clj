(ns descent.core
  (:require [descent.pom-parser :as parser]
            [descent.dependency-graph :as g])
  (:use [clojure.pprint :only (pprint)])
  (:gen-class))

(defn -main
  "Given a pom.xml path, this will extract the project's dependencies, load
  them into Datomic (so they're viewable over time), generate a graph
  visualization and then print the graph to a file.

  Usage: descent.core: <directory with poms> <output-file> [optional arguments]"
  [poms-dir prefix output-file & args]
  (let [graph (g/build-graph poms-dir prefix)
        image (g/create-image graph)]
    (pprint graph)
    (g/save-image-to-file image output-file)
    (g/save-graph-to-file graph (str "project-dependencies.graph"))))
