(ns chimera.csv-to-xlsform-test
  (:require [clojure.java.io :refer [input-stream]]
            [clojure.set :refer [rename-keys]]
            [clojure.string :refer [split]]
            [dk.ative.docjure.spreadsheet :as spreadsheet]
            [midje.sweet :refer :all]
            [chimera.csv-to-xlsform :refer :all]))

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

;;; Fixtures and expectations

(def example-csv (load-fixture "convert_test.csv"))
(def example-csv-list (split-csv example-csv))
(def example-csv-with-groups (load-fixture "convert_grouped_test.csv"))
(def example-csv-with-groups-list (-> example-csv-with-groups
                                      split-csv))
(def example-csv-eol (load-fixture "convert_test_eol.csv"))
(def filename "filename")
(def title "form title")
(def form-id "form_title")

(def example-schema
  (array-map
   "submit_data"    {:type :date}
   "food_type"      {:type :text}
   "description"    {:type :text}
   "amount"         {:type :decimal}
   "rating"         {:type :select_one
                     :options ["nasty"]}
   "risk_factor"    {:type :select_one
                     :options ["low_risk" "high_risk"]}
   "food_photo"     {:type :text}
   "location_name"  {:type :text}
   "location_photo" {:type :text}
   "gps"            {:type :geopoint}))

(def example-schema-strings
  (-> example-schema
      (update-in ["rating" :type] name)
      (update-in ["risk_factor" :type] name)))

(def example-group-schema+rows
  {:schema
   (clojure.set/rename-keys example-schema
                            {"description" "test/option[2]/description"
                             "food_type" "food/food_type"
                             "food_photo" "food/food_photo"})
   :rows example-csv-with-groups-list})

(def example-schema+rows {:rows example-csv-list
                          :schema example-schema})

(def expected-xls-sheet-data
  `(["survey" ("type" "name" "label"
                      "date" "submit_data" "submit_data"
                      "text" "food_type" "food_type"
                      "text" "description" "description"
                      "decimal" "amount" "amount"
                      "select_one rating" "rating" "rating"
                      "select_one risk_factor" "risk_factor" "risk_factor"
                      "text" "food_photo" "food_photo"
                      "text" "location_name" "location_name"
                      "text" "location_photo" "location_photo"
                      "geopoint" "gps" "gps")]
    ["choices" ("list name" "name" "label"
                            "rating" "nasty" "nasty"
                            "risk_factor" "low_risk" "low_risk"
                            "risk_factor" "high_risk" "high_risk")]
    ["settings" ("form_id" "title" ~form-id ~title)]))

(def example-illegal-char-csv (load-fixture "convert_special_char_test.csv"))

(def example-illegal-char-schema
  "When coming back accross the wire schema keys have been converted to
   keywords."
  (let [schema (:schema (-> example-illegal-char-csv csv->schema+rows))
        schema-keys (keys schema)]
    (rename-keys schema
                 (apply assoc {}
                        (interleave schema-keys
                                    (map keyword schema-keys))))))

(defn file->sheets+data
  "Convert an XLSForm file to a list of sheets and their data."
  [file-handle]
  (map
   (fn [x]
     ((juxt spreadsheet/sheet-name
            #(->> %
                  spreadsheet/cell-seq
                  (map spreadsheet/read-cell))) x))
   (-> file-handle
       input-stream
       spreadsheet/load-workbook
       spreadsheet/sheet-seq)))

;;; Facts

(facts "about csv->schema+rows"
       (fact "should produce the expected heuristic schema"
             (csv->schema+rows example-csv) => example-schema+rows)

       (fact "should produce the expected heuristic schema for csv with
              groups"
             (csv->schema+rows example-csv-with-groups)
             => example-group-schema+rows)

       (fact "should parse different single carriage return EOL"
             (csv->schema+rows example-csv-eol) => anything))

(facts "about schema->xlsform"
       (fact "should set the filename"
             (:filename (schema->xlsform filename
                                         example-schema-strings
                                         form-id
                                         title))
             => filename)
       (fact "should set the filename"
             (:column-names (schema->xlsform filename
                                             example-schema-strings
                                             form-id
                                             title))
             => (map #(nth % 1)
                     (-> example-schema schema->survey-sheet rest)))
       (fact "should set the filename"
             (-> (schema->xlsform filename
                                  example-schema-strings
                                  form-id
                                  title)
                 :tempfile
                 file->sheets+data) => expected-xls-sheet-data))

(facts "about schema->survey-sheet"
       (fact "should handle illegal characters"
             (schema->survey-sheet example-illegal-char-schema) => anything))

(facts "about merge-types-into-vector-schema"
       (fact "should handle types arg as a string"
             (merge-types-into-vector-schema {:a {:type :old}} "new")
             => {:a {:type "new"}}))

(facts "about rename-header-columns"
       (fact "should replace header row if first-row-as-labels? true"
             (rename-header-columns "b\n1\n2" ["a"] true) => "a\n1\n2")

       (fact "should work with \r\n separators"
             (rename-header-columns "b\r\n1\r\n2" ["a"] true) => "a\n1\n2")

       (fact "should work with \r separators"
             (rename-header-columns "b\r1\r2" ["a"] true) => "a\n1\n2")

       (fact "should add header row if first-row-as-labels? false"
             (rename-header-columns "1\n2" ["a"] false) => "a\n1\n2"))
