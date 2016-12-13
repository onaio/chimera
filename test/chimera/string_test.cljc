(ns chimera.string-test
  #?(:cljs (:require-macros [cljs.test :refer [is deftest testing]]))
  (:require [chimera.string :as string]
            #?(:clj [clojure.test :as t :refer [is deftest testing]]
               :cljs [cljs.test :as t])))

(deftest safe-lower-case-tests
  (testing "nil should return self"
    (is (= (string/safe-lower-case nil) nil)))

  (testing "non-string should return self"
    (is (= (string/safe-lower-case 1) nil)))

  (testing "string should return lower case"
    (is (= (string/safe-lower-case "STRING") "string"))))
