(ns descent.dependency-graph-test
  (:require [midje.sweet :refer :all]
            [clojure.java.io :as io]
            [descent.pom-parser :as parser]
            [descent.dependency-graph :as g]))

(fact "Can build graph from single pom"
      (let [path-to-pom (-> "poms/test_pom.xml" io/resource .getPath)
            pom-file (io/file path-to-pom)
            deps (parser/process-pom pom-file "")]
        (g/create-graph deps)) =>

      {"projectname-projectversion" ["dep4-2.0" "dep1-1.0.0" "dep2-1.0.1" "dep3-1.0.2"],
       "dep1-1.0.0" [],
       "dep4-2.0" [],
       "dep2-1.0.1" [],
       "dep3-1.0.2" []})

(fact "Can combine two pom dependency graphs"
      (let [pom (-> "poms/test_pom.xml" io/resource .getPath)
            pom1 (-> "poms/test_pom_1.xml" io/resource .getPath)
            pom-file (io/file pom)
            pom-file1 (io/file pom1)
            deps (parser/process-pom pom-file "")
            deps1 (parser/process-pom pom-file1 "")
            g (g/create-graph deps)
            g1 (g/create-graph deps1)]
        (g/merge-graphs g g1)) =>

      {"projectname-projectversion" ["dep4-2.0" "dep1-1.0.0" "dep2-1.0.1" "dep3-1.0.2"],
       "dep1-1.0.0" ["dep5-1.2"],
       "dep4-2.0" [],
       "dep2-1.0.1" [],
       "dep5-1.2" [],
       "dep3-1.0.2" []})

(fact "Can combine two graphs"
      (let [path-to-poms (-> "poms" io/resource .getPath)]
        (g/build-graph path-to-poms "")) =>

      {"projectname-projectversion" ["dep4-2.0" "dep1-1.0.0" "dep2-1.0.1" "dep3-1.0.2"],
       "dep1-1.0.0" ["dep5-1.2"],
       "dep4-2.0" [],
       "dep2-1.0.1" [],
       "dep5-1.2" [],
       "dep3-1.0.2" []})

(fact "Can combine two graphs with dependency filtering"
      (let [path-to-poms (-> "poms" io/resource .getPath)]
        (g/build-graph path-to-poms "com.acme")) =>

      {"dep1-1.0.0" [],
       "projectname-projectversion" ["dep1-1.0.0" "dep2-1.0.1"],
       "dep2-1.0.1" []})
