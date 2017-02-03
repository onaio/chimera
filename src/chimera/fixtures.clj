(ns chimera.fixtures
  (:require [chimera.csv-to-xlsform :refer [csv->schema+rows]]
            [chimera.test-helpers :refer [load-fixture split-csv]]
            [clojure.set :refer [rename-keys]]))

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
