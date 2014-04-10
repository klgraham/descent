(ns descent.pom-parser-test
  (:require [clojure.java.io :as io]
            [descent.pom-parser :as parser]
            [midje.sweet :refer :all]))

(def path-to-pom (-> "poms/test_pom.xml" io/resource .getPath))
(def pom-file (io/file path-to-pom))

(def pom (parser/load-pom path-to-pom))

(fact "Can get project name"
      (parser/get-library-name-from-pom pom) => "projectname")

(fact "Can get project version"
      (parser/get-library-version-from-pom pom) => "projectversion")

(fact "Can tell if pom has properties section"
      (parser/pom-has-properties? pom) => true)

(fact "Can tell if pom has dependencyManagement section"
      (parser/pom-has-dependencyManagement? pom) => true)

(fact "Can tell if pom has dependencies section"
      (parser/pom-has-dependencies? pom) => true)

(fact "Can get the properties from the pom"
      (parser/get-properties-from-pom pom) =>

      [{:tag :dep1.version, :attrs nil, :content ["1.0.0"]},
       {:tag :dep2.version, :attrs nil, :content ["1.0.1"]},
       {:tag :dep3.version, :attrs nil, :content ["1.0.2"]}
       ])

(parser/group-id-starts-with? "com.acme" "com.acme.dep2")

(fact "Can tell if a groups id contains the desired prefix"
      (parser/group-id-starts-with? "com.acme" "com.acme.dep2") => true
      (parser/group-id-starts-with? "" "com.acm.dep2") => true
      (parser/group-id-starts-with? "com.acme" "com.acm.dep2") => false)

(fact "Can tell if a dependency's group-id starts with a given prefix"
      (let [d {:tag :dependency,
               :attrs nil,
               :content [{:tag :groupId, :attrs nil, :content ["com.acme.dep2"]}
                         {:tag :artifactId, :attrs nil, :content ["dep2-artifact"]}
                         {:tag :type, :attrs nil, :content ["tar.gz"]}
                         {:tag :version, :attrs nil, :content ["${dep2.version}"]}]}]
        (parser/dep-has-prefix? "com.acme" d)) => true)


(fact "Can get dependencies from dependencyManagement"
      (parser/get-deps-from-dependency-management pom) =>

      [{:tag :dependency,
        :attrs nil,
        :content [{:tag :groupId, :attrs nil, :content ["com.acme.dep1"]}
                  {:tag :artifactId, :attrs nil, :content ["dep1-artifact"]}
                  {:tag :version, :attrs nil, :content ["${dep1.version}"]}
                  {:tag :type, :attrs nil, :content ["tar.gz"]}]}
       {:tag :dependency,
        :attrs nil,
        :content [{:tag :groupId, :attrs nil, :content ["com.acme.dep2"]}
                  {:tag :artifactId, :attrs nil, :content ["dep2-artifact"]}
                  {:tag :type, :attrs nil, :content ["tar.gz"]}
                  {:tag :version, :attrs nil, :content ["${dep2.version}"]}]}
       {:tag :dependency,
        :attrs nil,
        :content [{:tag :groupId, :attrs nil, :content ["com.thirdParty.dep3"]}
                  {:tag :artifactId, :attrs nil, :content ["dep3-artifact"]}
                  {:tag :version, :attrs nil, :content ["${dep3.version}"]}]}
       ])


(fact "Can filter out dependencies that don't start with a given prefix"
      (let [deps (parser/get-deps-from-dependency-management pom)]
        (parser/filter-by-group-id-prefix "com.acme" deps)) =>

      [{:tag :dependency,
        :attrs nil,
        :content [{:tag :groupId, :attrs nil, :content ["com.acme.dep1"]}
                  {:tag :artifactId, :attrs nil, :content ["dep1-artifact"]}
                  {:tag :version, :attrs nil, :content ["${dep1.version}"]}
                  {:tag :type, :attrs nil, :content ["tar.gz"]}]}
       {:tag :dependency,
        :attrs nil,
        :content [{:tag :groupId, :attrs nil, :content ["com.acme.dep2"]}
                  {:tag :artifactId, :attrs nil, :content ["dep2-artifact"]}
                  {:tag :type, :attrs nil, :content ["tar.gz"]}
                  {:tag :version, :attrs nil, :content ["${dep2.version}"]}]}])


(fact "Can extract dependencies from dependencyManagement"
      (parser/process-dependency-management pom "") => {:dep1 "1.0.0", :dep2 "1.0.1", :dep3 "1.0.2"})

(fact "Can get dependencies from dependencies section"
      (parser/get-deps-from-dependencies-section pom) =>

      [{:tag :dependency,
        :attrs nil,
        :content [{:tag :groupId, :attrs nil, :content ["com.thirdParty.dep4"]}
                  {:tag :artifactId, :attrs nil, :content ["dep4-artifact"]}
                  {:tag :version, :attrs nil, :content ["2.0"]}]} ])

(fact "Can extract dependencies from dependencies"
      (parser/process-dependencies-section pom "") => {:dep4 "2.0"})

(fact "Can extract dependencies from pom"
      (parser/process-pom pom-file "") =>

      {:project-name "projectname",
       :project-version "projectversion",
       :dependencies {:dep4 "2.0",
                      :dep1 "1.0.0",
                      :dep2 "1.0.1",
                      :dep3 "1.0.2"}})
