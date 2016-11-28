(ns chimera.string
  (:require [chimera.core :refer [not-nil?]]))

(defn is-null?
  "Checks if a variable is null"
  [s]
  (= "null" s))

(def is-not-null?
  (complement is-null?))

(defn ^Boolean substring?
  "True if substring is a substring of string"
  [substring string & {:keys [case-sensitive?] :or {case-sensitive? true}}]
  (if (or (empty? string)
          #?(:cljs (is-null? string)))
    false
    (not-nil? (re-find (re-pattern
                        (str (when-not case-sensitive? "(?i)") substring))
                       string))))
