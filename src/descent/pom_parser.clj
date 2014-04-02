(ns descent.pom-parser
  (:use [clojure.xml :only (parse)]))


;;;; Access the project dependencies through the following logic:
;;;;   1. if properties section exists, place versions in a hash-map
;;;;   2. if dependencyManagement section exists, extract deps if it does
;;;;      if dependencyManagement is absent, just process dependencies instead
;;;;   3. extract version nmumbers from dependencies and if needed use the properties map


;;; Load the pom.xml file and get basic info from it

(defn load-pom [pom-location] (parse (java.io.File. pom-location)))


(defn- get-library-name-from-pom
  "Given a pom, extract the project name."
  [pom]
  (-> pom :content (nth 2) :content first))


(defn- get-library-version-from-pom
  "Given a pom, extract the project version."
  [pom]
  (-> pom :content (nth 5) :content first))


;;; Functions to check if certain pom sections exist

(defn- tag-equals? [m tag] (= (:tag m) tag))


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


(defn- get-dependencies-from-properties
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

(defn- get-deps-from-dependency-management
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
          keyword))))


(defn- parse-group-id
  [gid]
  (-> (clojure.string/split gid #"\.")
      last keyword))


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


(defn- process-dependency-management
  [pom]
  (if (pom-has-dependencyManagement? pom)
    (let [properties (get-dependencies-from-properties pom)
          deps (get-deps-from-dependency-management pom)
          deps-seq (map #(make-dependency-version-pair % properties) deps)]
      (into {} deps-seq))
    nil))


;;; Functions to process dependencies

(defn- get-deps-from-dependencies-section
  "Given a pom, extract the dependencies from dependencies section."
  [pom]
  (-> (filter #(tag-equals? % :dependencies) (:content pom))
      first :content))


(defn- process-dependencies-section
  [pom]
  (if (pom-has-dependencies? pom)
    (let [properties (get-dependencies-from-properties pom)
          deps (get-deps-from-dependencies-section pom)
          deps-seq (map #(make-dependency-version-pair % properties) deps)]
      (into {} deps-seq))
    nil))


;;; Process entire pom

(defn process-pom
  "Given a pom file, extract it's dependencies and versions and place in a hash-map."
  [path-to-pom]
  (let [pom (load-pom path-to-pom)
        project-name (get-library-name-from-pom pom)
        version (get-library-version-from-pom pom)
        dep-management (process-dependency-management pom)
        deps (process-dependencies-section pom)
        dependencies (merge dep-management deps)]
    {:project-name project-name :project-version version :dependencies dependencies}))





