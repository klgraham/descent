(ns descent.pom-parser
  (:use [clojure.xml :only (parse)])
  (:require [clojure.java.io :as io]))

(defn load-pom [pom-location] (parse (java.io.File. pom-location)))

;(def pom (load-pom "resources/pom.xml"))

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

(defn- get-properties-from-pom
  "Given a pom, extract the properties and see if they contain dependency version data."
  [pom]
  (-> pom :content (nth 7) :content))

;(get-properties-from-pom pom)

;(def properties (get-properties-from-pom pom))
;(first properties)

(defn- contains-version?
  "Checks a given member of the pom properties to see if it contains version number."
  [coll]
  (let [tag (:tag coll)
        has-version? (-> (clojure.string/split (str tag) #"\.")
                         reverse
                         first
                         (= "version"))]
    has-version?))

;(contains-version? {:tag :apple.hh})
;(contains-version? {:tag :rivulet.version})

(defn- filter-out-properties-without-version
  "Returns only those properties that are a version number."
  [coll]
  (filter contains-version? coll))

;(filter-out-properties-without-version properties)


(defn- get-dep-version-map
  "Given the properties portion of a pom, return a seq of "
  [properties]
  (let [version-props (filter-out-properties-without-version properties)
        parse-name (fn [k](-> (clojure.string/split (str k) #"\.")
                              first
                              (subs 1)))]
    (-> (fn [x] [(parse-name (:tag x)) (-> x :content first)])
        (map version-props))))

;(get-dep-version-map properties)


(defn get-deps-and-versions
  [pom]
  (-> (get-properties-from-pom pom)
      get-dep-version-map))

;(get-deps-and-versions pom)



