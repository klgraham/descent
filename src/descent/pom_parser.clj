(ns descent.pom-parser
  (:use [clojure.xml :only (parse)])
  (:require [clojure.java.io :as io]))

(defn load-pom [pom-location] (parse (java.io.File. pom-location)))

(def pom (load-pom "resources/pom.xml"))

;(:tag pom)
;(:attrs pom)

;; :content once takes us to the 1st level of data
;(:content pom)

;(def library (-> pom :content (nth 2) :content first))
;(def version (-> pom :content (nth 5) :content first))

(defn- get-library-name-from-pom
  "Given a pom, extract the project name."
  [pom]
  (-> pom :content (nth 2) :content first))

(defn- get-library-version-from-pom
  "Given a pom, extract the project version."
  [pom]
  (-> pom :content (nth 5) :content first))

;(get-properties-from-pom pom)

;(def properties (get-properties-from-pom pom))
;(first properties)



;(contains-version? {:tag :apple.hh})
;(contains-version? {:tag :rivulet.version})

;(filter-out-properties-without-version properties)


;;;; Access the project dependencies through the following logic:
;;;;   1. if properties section exists, place versions in a hash-map
;;;;   2. if dependencyManagement section exists, extract deps if it does
;;;;      if dependencyManagement is absent, just process dependencies instead
;;;;   3. extract version nmumbers from dependencies and if needed use the properties map

;;; Functions to check if certain pom sections exist

(defn- tag-equals? [m tag] (= (:tag m) tag))

;(get-in pom [:content])

(defn- pom-has-properties?
  [pom]
  (-> (filter #(tag-equals? % :properties) (:content pom))
      count
      (> 0)))


(defn- pom-has-dependencyManagement?
  [pom]
  (-> (filter #(tag-equals? % :dependencyManagement) (:content pom))
      count
      (> 0)))


(defn- pom-has-dependencies?
  [pom]
  (-> (filter #(tag-equals? % :dependencies) (:content pom))
      count
      (> 0)))


;;; Functions to process the properties section

(defn- contains-version?
  "Checks a given member of the pom properties to see if it contains version number."
  [coll]
  (let [tag (:tag coll)
        has-version? (-> (clojure.string/split (str tag) #"\.")
                         reverse
                         first
                         (= "version"))]
    has-version?))


(defn- filter-out-properties-without-version
  "Returns only those properties that are a version number."
  [coll]
  (filter contains-version? coll))


(defn- get-properties-from-pom
  "Given a pom, extract the properties and see if they contain dependency version data."
  [pom]
  (-> (filter #(tag-equals? % :properties) (:content pom))
      first
      :content
      filter-out-properties-without-version))


(defn- parse-name
  [name]
  (-> (clojure.string/split (str name) #"\.")
      first
      (subs 1)))


(defn get-dependencies-from-properties
  "Given the properties portion of a pom, return a seq of "
  [pom]
  (if (pom-has-properties? pom)
    (let [properties (get-properties-from-pom pom)
          properties-map (transient {})]
      (doseq [p properties]
        (assoc! properties-map (keyword (parse-name (:tag p))) (first (:content p))))
      (persistent! properties-map))
    nil))

(get-dependencies-from-properties pom)


;;; Functions to process dependencyManagement

(defn- get-deps-from-dependency-management
  "Given a pom, extract the dependencies from dependencyManagement."
  [pom]
  (-> (filter #(tag-equals? % :dependencyManagement) (:content pom))
      first :content first :content))

(get-deps-from-dependency-management pom)


(defn- first-char-is-$? [s] (= "$" (subs s 0 1)))


(defn- parse-version-variable
  [version]
  (let [length (count version)]
    (if (first-char-is-$? version)
      (-> (subs version 2)
          (subs 0 (- length 3))
          (clojure.string/split #"\.")
          first
          keyword))))

(parse-version-variable "${dep1.version}")

(defn- parse-group-id
  [gid]
  (-> (clojure.string/split gid #"\.")
      last keyword))

(parse-group-id "a.b.c")

(defn- make-dependency-version-pair
  [m properties]
  (let [{:keys [tag attrs content]} m
        group-id-map (first (filter #(tag-equals? % :groupId) content))
        group-id (first (:content group-id-map))
        version-map (first (filter #(tag-equals? % :version) content))
        version (first (:content version-map))]
    [(parse-group-id group-id)
     (if (first-char-is-$? version)
       (get properties (parse-version-variable version))
       version)]))


(def x {:tag :dependency
        :attrs nil
        :content [{:tag :groupId
                   :attrs nil
                   :content ["com.company.dep1"]}

                  {:tag :artifactId
                   :attrs nil
                   :content ["dep1-core"]}

                  {:tag :version
                   :attrs nil
                   :content ["${dep1.version}"]}

                  {:tag :type
                   :attrs nil
                   :content ["tar.gz"]}]})

(make-dependency-version-pair x {:dep1 "1.0.0-SNAPSHOT"})

(defn process-dependency-management
  [pom]
  (if (pom-has-dependencyManagement? pom)
    (let [properties (get-dependencies-from-properties pom)
          deps (get-deps-from-dependency-management pom)
          deps-seq (map #(make-dependency-version-pair % properties) deps)]
      (into {} deps-seq))
    nil))

(process-dependency-management pom)


;;; Functions to process dependencies

