(ns descent.dependency-graph
  (use 'rhizome.viz))


;;;; Given a map of a project's dependencies, and versions, create a graph
;;;; to visualize the dependencies.

(defn create-graph
  [{:keys [project-name version dependencies]}]
  (let [dep-names (into [] (keys dependencies))
        nodes (hash-map (keyword project-name) dep-names)
        dep-nodes (zipmap dep-names (repeat (count dep-names) []))]
    (merge nodes dep-nodes)))


;(view-graph (keys g) g
;    :node->descriptor (fn [n] {:label n}))
