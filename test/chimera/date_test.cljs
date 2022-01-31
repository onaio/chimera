(ns chimera.date-test
  (:require-macros [cljs.test :refer (is deftest testing)])
  (:require [chimera.date :refer [format-date]]))

(deftest compositions-api
  (testing "Returns the input if js/Date finds the date to be invalid"
    (is (= (format-date "")                         ""))
    (is (= (format-date "2014 04 25")               "Apr 25, 2014"))
    (is (= (format-date "2014 04 25 to 2014 06 13") "2014 04 25 to 2014 06 13"))
    (is (= (format-date "2014-04-25T01:32:21.196Z") "Apr 25, 2014"))
    (is (= (format-date "11:12")                    "11:12"))))
