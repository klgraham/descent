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
  (some? (filter #(tag-equals? % :properties) (:content pom))))


(defn- pom-has-dependencyManagement?
  [pom]
  (some? (filter #(tag-equals? % :dependencyManagement) (:content pom))))


(defn- pom-has-dependencies?
  [pom]
  (some? (filter #(tag-equals? % :dependencies) (:content pom))))


;;; Functions to process the properties section

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

;(get-properties-from-pom pom)

(defn- contains-version?
  "Checks a given member of the pom properties to see if it contains version number."
  [coll]
  (let [tag (:tag coll)
        has-version? (-> (clojure.string/split (str tag) #"\.")
                         reverse
                         first
                         (= "version"))]
    has-version?))


(defn- parse-name
  [name]
  (-> (clojure.string/split (str name) #"\.")
      first
      (subs 1)))


(defn- get-dep-version-map
  "Given the properties portion of a pom, return a seq of "
  [properties]
  (let [properties-map (transient {})]
    (doseq [p properties]
      (assoc! properties-map (keyword (parse-name (:tag p))) (first (:content p))))
    (persistent! properties-map)))

(get-dep-version-map (get-properties-from-pom pom))


;;; Functions to process dependencyManagement

;;; Functions to process dependencies
