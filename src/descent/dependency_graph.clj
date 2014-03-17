(ns descent.dependency-graph
  (use 'rhizome.viz))


;;;; Given a map of a project's dependencies, and versions, create a graph
;;;; to visualize the dependencies.

(defn create-graph
  "Places dependencies into a hash-map form used by Rhizome."
  [{:keys [project-name version dependencies]}]
  (let [name (str project-name + "-" version)
        dep-names (into [] (map #(keyword (str (key %) "-" (val %))) dependencies))
        nodes (hash-map (keyword name) dep-names)
        dep-nodes (zipmap dep-names (repeat (count dep-names) []))]
    (merge nodes dep-nodes)))


;(view-graph (keys g) g
;    :node->descriptor (fn [n] {:label n}))

(defn create-image
  [g]
  (graph->image (keys g) g
                :node->descriptor (fn [n] {:label n})))


(defn save-image-to-file
  "Writes the image to a .png file."
  [image file]
  (save-image image file))

(defn save-graph-to-file [g file] (spit file g))


(defn load-graph-from-file
  "Reads in a graph in its hash-map form. Returns a hash-map."
  [file] (load-file file))


(defn merge-graphs [g1 g2] (merge g1 g2))
