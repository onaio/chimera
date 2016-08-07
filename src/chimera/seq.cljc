(ns chimera.seq)

(defn has-keys?
  "True is map has all these keys."
  [m keys]
  (every? (partial contains? m) keys))

(defn in?
  "True if elem is in list, false otherwise."
  [list elem]
  (boolean (some #(= elem %) list)))

(defn mapply [f & args] (apply f (apply concat (butlast args) (last args))))

(def select-values (comp vals select-keys))
