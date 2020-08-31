(ns chimera.csv-to-xlsform-test
  (:require [clojure.java.io :refer [input-stream]]
            [dk.ative.docjure.spreadsheet :as spreadsheet]
            [midje.sweet :refer :all]
            [chimera.fixtures :refer [example-csv
                                      example-csv-eol
                                      example-csv-with-groups
                                      example-illegal-char-schema
                                      example-group-schema+rows
                                      example-schema
                                      example-schema+rows
                                      example-schema-strings
                                      expected-xls-sheet-data
                                      filename
                                      form-id
                                      title]]
            [chimera.csv-to-xlsform :refer :all]))

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
             (merge-types-into-vector-schema {:a {:type :old}} "new" [["new"]])
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
