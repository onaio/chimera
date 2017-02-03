(ns chimera.test-helpers
  (:require [clojure.string :refer [split]]))

;;; Fixture loading

(def fixture-path "test/fixtures/")

(defn load-fixture
  "Load fixture by name from fixture path."
  [x]
  (slurp (str fixture-path x)))

(defn split-csv
  "Split a CSV string's rows and columns"
  [csv-str]
  (map #(split % #",") (split csv-str #"\n")))
