(ns descent.dependency-graph
  (:use [rhizome.viz])
  (:require [descent.pom-parser :as parser]))



;;;; Given a map of a project's dependencies, and versions, create a graph
;;;; to visualize the dependencies.

(defn create-graph
  "Places dependencies into a hash-map form used by Rhizome."
  [{:keys [project-name project-version dependencies]}]
  (let [graph-name (str project-name "-" project-version)
        dep-names (into [] (map #(str (name (key %)) "-" (val %)) dependencies))
        nodes (hash-map graph-name dep-names)
        dep-nodes (zipmap dep-names (repeat (count dep-names) []))]
    (merge nodes dep-nodes)))


;(view-graph (keys g) g
;    :node->descriptor (fn [n] {:label n}))

(defn create-image
  [g]
  (graph->image (keys g) g
                :node->descriptor (fn [n] {:label n})
                :vertical? false))


(defn save-image-to-file
  "Writes the image to a .png file."
  [image file]
  (save-image image file))

(defn save-graph-to-file [g file] (spit file g))


(defn load-graph-from-file
  "Reads in a graph in its hash-map form. Returns a hash-map."
  [file] (load-file file))


(defn merge-graphs
  "Merges two maps that were created by the create-graph function"
  ([] {})
  ([g1 g2] (merge-with (comp vec flatten conj) g1 g2)))

(defn build-graph
  "Given a directory containing at least two interrelated poms,
  builds a dependency graph for each and combines them into a
  single dependency graph."
  [directory prefix]
  (let [poms (-> (clojure.java.io/file directory)
                 file-seq
                 rest
                 vec)
        deps-maps (map #(parser/process-pom % prefix) poms)
        graphs (map create-graph deps-maps)]
    (reduce merge-graphs graphs)))
