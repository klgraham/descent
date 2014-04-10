(ns descent.pom-parser
  (:use [clojure.xml :only (parse)]))


;;;; Access the project dependencies through the following logic:
;;;;   1. if properties section exists, place versions in a hash-map
;;;;   2. if dependencyManagement section exists, extract deps if it does
;;;;      if dependencyManagement is absent, just process dependencies instead
;;;;   3. extract version nmumbers from dependencies and if needed use the properties map


;;; Load the pom.xml file and get basic info from it

(defn load-pom [pom-location] (parse (java.io.File. pom-location)))
(defn load-pom-file [file] (parse file))


(defn get-library-name-from-pom
  "Given a pom, extract the project name."
  [pom]
  (->> (:content pom)
       (filter #(= (:tag %) :name))
       first :content first
       clojure.string/lower-case))


(defn get-library-version-from-pom
  "Given a pom, extract the project version."
  [pom]
  (->> (:content pom)
       (filter #(= (:tag %) :version))
       first :content first
       clojure.string/lower-case))


;;; Functions to check if certain pom sections exist

(defn- tag-equals? [m tag] (= (:tag m) tag))


(defn pom-has-properties?
  [pom]
  (-> (filter #(tag-equals? % :properties) (:content pom))
      count
      (> 0)))


(defn pom-has-dependencyManagement?
  [pom]
  (-> (filter #(tag-equals? % :dependencyManagement) (:content pom))
      count
      (> 0)))


(defn pom-has-dependencies?
  [pom]
  (-> (filter #(tag-equals? % :dependencies) (:content pom))
      count
      (> 0)))


;;; Functions to process the properties section

(defn contains-version?
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


(defn get-properties-from-pom
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
          properties-map (atom {})]
      (doseq [p properties]
        ;(println (parse-name (:tag p)) " -> " (first (:content p)))
        (swap! properties-map assoc (keyword (parse-name (:tag p))) (first (:content p))))
      (deref properties-map))
    nil))


;;; Functions to process dependencyManagement

(defn get-deps-from-dependency-management
  "Given a pom, extract the dependencies from dependencyManagement."
  [pom]
  (-> (filter #(tag-equals? % :dependencyManagement) (:content pom))
      first :content first :content))


(defn- first-char-is-$? [s] (= "$" (subs s 0 1)))


(defn- parse-version-variable
  [version]
  (let [length (count version)]
    (if (first-char-is-$? version)
      (-> (subs version 2)
          (subs 0 (- length 3))
          (clojure.string/split #"\.")
          first
          clojure.string/lower-case
          keyword))))


(defn- parse-group-id
  [gid]
  (-> (clojure.string/split gid #"\.")
      last
      clojure.string/lower-case
      keyword))

(defn group-id-starts-with? [prefix group-id]
  "Given a group id prefix, returns true if the group id contains the prefix."
  (some? (re-find (re-pattern prefix) group-id)))

(defn dep-has-prefix?
  [prefix dep]
  (->> (:content dep)
       (filter #(= (:tag %) :groupId))
       first
       :content
       first
       (group-id-starts-with? prefix)))

(defn filter-by-group-id-prefix
  "Filters out all dependencies with a group id that doesn't match the prefix."
  [prefix deps]
  (vec (filter (partial dep-has-prefix? prefix) deps)))

(defn- make-dependency-version-pair
  [m properties]
  (let [{:keys [tag attrs content]} m
        group-id-map (first (filter #(tag-equals? % :groupId) content))
        group-id (-> (:content group-id-map) first clojure.string/lower-case)
        version-map (first (filter #(tag-equals? % :version) content))
        version (-> (:content version-map) first clojure.string/lower-case)]
    [(parse-group-id group-id)
     (if (first-char-is-$? version)
       (get properties (parse-version-variable version))
       version)]))


(defn process-dependency-management
  [pom prefix]
  (if (pom-has-dependencyManagement? pom)
    (let [properties (get-dependencies-from-properties pom)
          deps (get-deps-from-dependency-management pom)
          filtered-deps (filter-by-group-id-prefix prefix deps)
          deps-seq (map #(make-dependency-version-pair % properties) filtered-deps)]
      (into {} deps-seq))
    nil))


;;; Functions to process dependencies

(defn get-deps-from-dependencies-section
  "Given a pom, extract the dependencies from dependencies section."
  [pom]
  (-> (filter #(tag-equals? % :dependencies) (:content pom))
      first :content))


(defn process-dependencies-section
  [pom prefix]
  (if (pom-has-dependencies? pom)
    (let [properties (get-dependencies-from-properties pom)
          deps (get-deps-from-dependencies-section pom)
          filtered-deps (filter-by-group-id-prefix prefix deps)
          deps-seq (map #(make-dependency-version-pair % properties) filtered-deps)]
      (into {} deps-seq))
    nil))


;;; Process entire pom

(defn- parse-project-name [project-name]
  (-> (clojure.string/split project-name #"::")
      first
      clojure.string/trim
      clojure.string/lower-case))

(defn parse-pom-dependencies
  "Given a pre-loaded pom file, extract it's dependencies and versions and place in a hash-map.
  Can also filter out dependencies with group id that doesn't start with a given prefix"
  [pom prefix]
  (let [project-name (get-library-name-from-pom pom)
        version (get-library-version-from-pom pom)
        dep-management (process-dependency-management pom prefix)
        deps (process-dependencies-section pom prefix)
        dependencies (merge dep-management deps)]
    {:project-name (parse-project-name project-name)
     :project-version version
     :dependencies dependencies}))

(defn process-pom
  "Given a pom file, extract it's dependencies and versions and place in a hash-map."
  [pom-file prefix]
  (let [pom (load-pom-file pom-file)]
    (parse-pom-dependencies pom prefix)))
