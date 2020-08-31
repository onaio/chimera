(ns chimera.csv-to-xlsform
  (:import [java.io File])
  (:require [chimera.seq :refer [duplicates in? transpose]]
            [clj-time.format :as f]
            [clj-time.local :as l]
            [clojure-csv.core :refer [parse-csv]]
            [clojure.java.io :as io]
            [clojure.string :as string :refer [includes? join split]]
            [dk.ative.docjure.spreadsheet :as spreadsheet]
            [slingshot.slingshot :refer [throw+]]))

(def expected-geopoint-length 3)
(def illegal-chars-regex #"\[|\]|\/|:|;|\?| ")
(def na-strings ["n/a" ""])
(def num-preview-rows 5)
(def proportion-unique 0.5)

(defn rename-header-columns
  "Takes a CSV file represented as a string and add the header column
   with the names in vector `column-names`. If `first-row-as-labels?` is true
   this replaces the first row, otherwise this row is prepended."
  [csv-str column-names first-row-as-labels?]
  (let [header-row (join "," column-names)]
    (if first-row-as-labels?
      (join "\n" (cons header-row (rest (string/split csv-str #"\r\n|\r|\n"))))
      (str header-row "\n" csv-str))))

(defn- build-default-header
  "Create a default header matching the number of columns in an assumed data
   only CSV"
  [rows]
  ;; needs translation?
  (map #(str "column %") (range 1 (-> rows count inc))))

(defn parse-csv-data [csv has-header?]
  (let [parsed-csv (parse-csv csv)
        parsed-csv (if (includes? (join (flatten parsed-csv)) "\r")
                     (parse-csv csv :end-of-line "\r")
                     parsed-csv)
        ;; removes blank rows
        csv-vector (remove (fn [r] (and (= (count r) 1)
                                        (string/blank? (first r))))
                           parsed-csv)
        [header & rows] (when has-header?
                          (cond->> csv-vector
                            (not has-header?)
                            (cons (build-default-header csv-vector))))
        num-columns (count header)
        columns (if rows (transpose rows) (repeat num-columns []))]
    {:columns columns
     :num-columns num-columns
     :header header
     :rows rows}))

(defn merge-types-into-vector-schema
  "Merge a list of types into the :types value in an vectorized schema.
   Returns an array-map."
  [schema types-raw columns]
  (let [types (cond-> types-raw
                (not ((some-fn seq? vector?) types-raw)) vector)]
    (apply array-map
           (flatten (map-indexed (fn [idx [label opts]]
                                   (let [indexed-type (nth types idx)]
                                     [label
                                      (cond->
                                       (assoc opts :type indexed-type)
                                        (= "select_one" indexed-type)
                                        (assoc :options (->> (nth columns idx)
                                                             distinct
                                                             vec)))]))
                                 schema)))))

(defn format-xlsform-names
  "Replace characters that are illegal in XLSForm names with underscores."
  [s]
  (string/replace s illegal-chars-regex "_"))

(defmacro can-map-any-truthy?
  "If applying f to any member of v raises the exception e, or if all
   applications of f are falsey, return false. Otherwise return true."
  [f v e]
  `(try (not-every? #(or (false? %) (nil? %)) (doall (map ~f ~v)))
        (catch ~e _# false)))

(defn- is-geopoint-str?
  "Return true if this string looks like a geopoint string, otherwise return
   false or raise a NumberFormatException.

   Geopoint strings have 3 values, as 'latitude longitude altitude', with
   (south pole) -90 <= latitude <= 90 (north pole) and
   (Prime Meridian) 0 <= longitude <= 180 (near the International Date Line)."
  [s]
  (when-let [geopoint (split s #" ")]
    (when (= (count geopoint) expected-geopoint-length)
      (let [[latitude longitude] (map bigdec geopoint)]
        (and (<= latitude 90) (>= latitude -90)
             (<= longitude 180) (>= longitude 0))))))

(defn category-like?
  "Return true if v looks like a category. We expect v is a category if the
   proportion of distinct values is less than the constant proportion-unique,
   where we ignore blanks or values that we think represent n/a strings."
  [v & {:keys [na-strings]}]
  (let [v-no-na (cond->> v na-strings (remove #(in? na-strings %)))
        denom (count v-no-na)]
    (and (-> denom zero? not)
         (< (/ (count (distinct v-no-na)) denom) proportion-unique))))

(defn- column->type
  "Return a guessed type for a vector of column data. Default to text."
  [v]
  (cond
    (can-map-any-truthy? bigdec v NumberFormatException) :decimal
    (can-map-any-truthy? #(f/parse (f/formatters :date) %)
                         v
                         IllegalArgumentException) :date
    (can-map-any-truthy? is-geopoint-str? v NumberFormatException) :geopoint
    (category-like? v :na-strings na-strings) :select_one
    :else
    :text))

(defn- create-timestamp
  "Return a current timestamp as a string."
  []
  (f/unparse (f/formatters :basic-date-time) (l/local-now)))

(defn- create-workbook
  "Create a new XLSX workbook.  Sheet-name is a string name for the sheet. Data
  is a vector of vectors, representing the rows and the cells of the rows.
  Alternate sheet names and data to create multiple sheets.
  (create-workbook \"SheetName1\" [[\"A1\" \"A2\"][\"B1\" \"B2\"]]
                   \"SheetName2\" [[\"A1\" \"A2\"][\"B1\" \"B2\"]] "
  [sheet-name data & name-data-pairs]
  ;; incomplete pairs should not be allowed
  {:pre [(even? (count name-data-pairs))]}
  ;; call single arity version to create workbook
  (let [workbook (spreadsheet/create-workbook sheet-name data)]
    ;; iterate through pairs adding sheets and rows
    (doseq [[s-name data] (partition 2 name-data-pairs)]
      (-> workbook
          (spreadsheet/add-sheet! s-name)
          (spreadsheet/add-rows!  data)))
    workbook))

(defn- schema->choices-list
  "Take list of maps with the keys as column names and the values as
   another map with the keys type and options. Convert this schema into a
   list of choices formatted for an XLSForm."
  [schema]
  (reduce (fn [l field]
            (let [[label {:keys [options type]}] field]
              (cond-> l
                (= type "select_one")
                (concat
                 (reduce #(conj %1
                                [(-> label name format-xlsform-names) %2 %2])
                         []
                         options)))))
          []
          schema))

(defn- build-schema
  "Build a ordered array-map from column name to a schema map.

   The schema map looks like:

   {:type :z-column-type
    :options [\"z\" \"list\" \"of\" \"distinct\" \"column\" \"values\"]}

   where :options is ONLY included if :type is :select_one."
  [num-columns header columns]
  (apply array-map
         (flatten
          (for [i (range num-columns)
                :let [label (nth header i)
                      column (nth columns i)
                      column-type (column->type column)]]
            [label (cond-> {:type column-type}
                     (= column-type :select_one)
                     (assoc :options (remove empty? (distinct column))))]))))

(defn csv->schema+rows
  "Takes a CSV string and returns a map from schema to the CSV schema and rows
   to the first `num-preview-rows` in the CSV.

   Optional arguments:
     :has-header - if true (default) we assume the first row
       is not data and ignore it.
     :duplicate-mode - describes what to do when encountering duplicate column
       names. Only current option is :fail (default), which throws an
       exception. If needed we could add :rename or :first options."
  [csv & {:keys [has-header? duplicate-mode] :or {has-header? true
                                                  duplicate-mode :fail}}]
  (let [{:keys [num-columns header columns rows]}
        (parse-csv-data csv has-header?)]
    (when (not= (distinct header) header)
      ;; There are duplicate columns
      (case duplicate-mode
        :fail (throw+ {:duplicate-columns (duplicates header)})))
    {:schema (build-schema num-columns header columns)
     :rows (cons header (take num-preview-rows rows))}))

(defn schema->survey-sheet
  "Convert a schema into the survey sheet in an XLSForm."
  [schema]
  (cons ["type" "name" "label"]
        (for [[column-label {column-type :type}] schema
              :let [column-name (format-xlsform-names column-label)]]
          [(cond-> column-type
             true name
             (= column-type "select_one") (str " " column-name))
           column-name
           column-label])))

(defn schema->xlsform
  "Schema that is list of maps with the keys as column names and the values as
   another map with the keys type and options. Returns an XLSForm for that
   schema. Verifies that all the types are valid for an XLSForm."
  [filename schema form-id title]
  (let [survey-sheet (schema->survey-sheet schema)
        wb (create-workbook "survey"
                            survey-sheet
                            "choices"
                            (cons
                             ["list name" "name" "label"]
                             (schema->choices-list schema))
                            "settings"
                            [["form_id" "title"]
                             [form-id title]])
        temp-file (File/createTempFile
                   (str filename "-" (create-timestamp)) nil)
        wb-stream (io/output-stream temp-file)]
    (spreadsheet/save-workbook! wb-stream wb)
    {:column-names (map #(nth % 1) (rest survey-sheet))
     :filename     filename
     :tempfile     temp-file}))
