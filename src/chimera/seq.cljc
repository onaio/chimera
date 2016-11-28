(ns chimera.seq
  (:require [clojure.string :as string]
            [chimera.string :refer [substring?]]
            [clojure.set :as clj-set]
            #?@(:clj  [[clj-time.core :as time]
                       [clj-time.local :as l]])))

(def concatv
  "Concat then turn into vector"
  (comp vec concat))

(def filter-first (comp first filter))

(def not-empty? (complement empty?))

(def select-values (comp vals select-keys))

(def select-value (comp first select-values))

#?(:clj
   (def default-date (time/date-time 1990 1 1)))

(defn duplicates
  "Return a list of all items that appear more than once in a sequence."
  [l]
  (keep #(when (-> % last (> 1)) (first %)) (frequencies l)))

(defn has-keys?
  "True is map has all these keys."
  [m keys]
  (every? (partial contains? m) keys))

(defn in?
  "True if elem is in list, false otherwise."
  [list elem]
  (boolean (some #(= elem %) list)))

(defn mapply [f & args] (apply f (apply concat (butlast args) (last args))))

(defn sort-by-category
  [sort-by-key collection]
  (sort-by
    (fn [item] (-> item :metadata sort-by-key))
    collection))

(defn update-values
  "Apply a function to each value of a map. From http://goo.gl/JdwzZf"
  [m f & args]
  (reduce (fn [r [k v]] (assoc r k (apply f v args))) {} m))

#?(:clj
   (defn sort-by-date
     [sort-by-key collection]
     (sort-by
       (fn [item]
         (l/to-local-date-time
           (if-let [sort-by-value (seq (sort-by-key item))]
             (string/join sort-by-value)
             ;; Use date in the past if no date
             default-date)))
       time/after?
       collection)))

(defn sort-by-name
  [sort-by-key collection]
  (sort-by #(-> % sort-by-key string/lower-case) collection))

#?(:clj
   (defn sort-collection
     "Sorts collection based on various keys"
     [sort-by-key collection]
     (condp some [sort-by-key]
       #{:category} :>> #(sort-by-category % collection)
       #{:date_created
         :last_submission_date
         :last_submission_time} :>> #(sort-by-date % collection)
       #{:name :title} :>> #(sort-by-name % collection)
       #{:num_of_submissions} :>> #(sort-by % collection)
       (sort-collection :date_created collection))))

(defn dissoc-vec
  "Removes elem from a vector by its position."
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

(defn assoc-in-multi
  "Like core/assoc-in but accepting multiple key vectors and values
   (assoc-in* coll & args)"
  [coll & args]
  {:pre (even? (count args))}
  (let [pairs (partition 2 args)
        reducer (fn [skeleton-map [keys value]]
                  (assoc-in skeleton-map keys value))]
    (reduce reducer coll pairs)))

(defn deep-merge
  "Deep merge any number of maps."
  [& maps]
  (apply merge-with (fn [x y]
                      (cond (map? y) (deep-merge x y)
                            (vector? y) (concat x y)
                            :else y))
         maps))

(defn fn-collection
  "Apply func to pred matching k value to v."
  [func k v collection]
  (if v (func #(= v (k %)) collection) collection))

(defn filter-collection
  "Keeps values in list maps where k value matchs v"
  [k v collection]
  (fn-collection filter k v collection))

(defn filter-out-collection
  "Removes values in list maps where k value matchs cond"
  [k v collection]
  (fn-collection remove k v collection))

(defn remove-falsey-values
  "Remove map entries where the value is falsey."
  [a-map]
  (into {} (remove (comp string/blank? second) a-map)))

(defn search-collection
  "Return collections with a key matching the query."
  ([query collection k]
   (search-collection query collection k true))
  ([query collection k case-insensitive?]
   (let [query-cased (if case-insensitive? (string/lower-case query) query)]
     (remove
       nil?
       (for [member collection
             :let [v (k member)
                   v-cased (if case-insensitive? (string/lower-case v) v)]
             :when (substring? query-cased v-cased)]
         member)))))

(defn positions
  "Returns the position of at which pred is true for items in coll."
  [pred coll]
  (keep-indexed (fn [idx x] (when (pred x) idx)) coll))

(defn index-of
  "Returns index of an item within a collection."
  [coll item]
  (first (positions #{item} coll)))

(defn update-map-in-list
  "Update map value in list based on key match"
  [list map-to-update key-to-match]
  (mapv #(if (= (key-to-match %) (key-to-match map-to-update)) map-to-update %)
        list))

(defn flatten-map
  "Basically lifted from http://stackoverflow.com/a/17902228/420386"
  ([form]
   (flatten-map form "/"))
  ([form separator]
   (into {} (flatten-map form separator nil)))
  ([form separator prefix]
   (mapcat (fn [[key value]]
             (let [full-prefix (if prefix
                                 (str prefix separator (name key))
                                 (name key))]
               (if (map? value)
                 (flatten-map value separator full-prefix)
                 [[full-prefix value]])))
           form)))

(defn diff
  "Return difference between 2 sequences."
  [a b]
  (clj-set/difference (set a) (set b)))

(defn ordered-diff
  "Return difference between 2 sequences. Preserves ordering in first seq."
  [a b]
  (filter #(not (contains? (set b) %)) a))

(defn union
  "Merges two sequeneces"
  [a b]
  (clj-set/union (set a) (set b)))

(defn remove-nil
  "Remove nil values from a sequence."
  [l]
  (remove nil? l))

(defn toggle
  "Removes x from coll if present, and adds if absent."
  [coll x]
  (if (contains? (set coll) x)
    (remove #(= x %) coll)
    (conj coll x)))

(defn indexed
  "Given a seq, produces a two-el seq. [a b c] => [[0 a] [1 b] [2 c]]."
  [coll]
  (map-indexed vector coll))

(defn transpose
  [m]
  (apply mapv vector m))

(defn map-list->map
  "Get the first map from a list of maps with the given value for key."
  [map-list k v]
  (filter-first #(= (k %) v) map-list))
