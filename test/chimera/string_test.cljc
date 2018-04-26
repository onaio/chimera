(ns chimera.string-test
  #?(:cljs (:require-macros [cljs.test :refer [is deftest testing]]))
  (:require [chimera.string :as string]
            [clojure.string :refer [join]]
            #?(:clj [clojure.test :as t :refer [is deftest testing]]
               :cljs [cljs.test :as t])))

(deftest safe-lower-case-tests
  (testing "nil should return self"
    (is (= (string/safe-lower-case nil) nil)))

  (testing "non-string should return self"
    (is (= (string/safe-lower-case 1) nil)))

  (testing "string should return lower case"
    (is (= (string/safe-lower-case "STRING") "string"))))

(deftest str-is-true?-tests
  (testing "str-is-true? returns true when given true string value"
    (is (= (string/str-is-true? "true") true)))

  (testing "str-is-true? returns false when given a non-true string value"
    (is (= (string/str-is-true? "yellow") false))))

(def valid-email "a@b.co")

(deftest is-email?-test
  (testing "valid email address"
    (is (= (string/is-email? valid-email) valid-email)))

  (testing "invalid email address"
    (is (= (string/is-email? "invalid") nil))))

(def long-string
  (join "" (repeat (+ string/truncate-if-longer-than 1) "a")))
(def longer-string
  (join "" (repeat (* string/truncate-if-longer-than 2) "a")))

(deftest truncate-with-ellipsis
  (testing "does not truncate short strings"
    (is (= (string/truncate-with-ellipsis "hello") "hello")))

  (testing "does truncate long strings"
    (is (not= (string/truncate-with-ellipsis long-string) long-string))
    (is (= (count (string/truncate-with-ellipsis longer-string))) 51)))

(deftest ends-with?
  (testing "handles empty string and/or suffix"
    (is (= (string/ends-with? "" "") true))
    (is (= (string/ends-with? "a" "") true))
    (is (= (string/ends-with? "" "a") false)))

  (testing "true if string ends with suffix"
    (is (= (string/ends-with? "a" "a") true))
    (is (= (string/ends-with? "abc" "abc") true))
    (is (= (string/ends-with? "abc" "bc") true))
    (is (= (string/ends-with? "abc" "c") true)))

  (testing "false if string does not end with suffix"
    (is (= (string/ends-with? "abc" "ab") false))
    (is (= (string/ends-with? "abc" "ac") false))
    (is (= (string/ends-with? "abc" "b") false))
    (is (= (string/ends-with? "abc" "a") false))))

(deftest substring?-tests
  (testing "case-insensitive substrings"
    (is (= (string/substring? "FO" "foo" :case-sensitive? false)
           true))
    (is (= (string/substring? "fo" "FOO" :case-sensitive? false)
           true)))
  (testing "null substrings"
    (is (= (string/substring? "" {})
           false))
    (is (= (string/substring? "" {} :case-sensitive? false))))
  (testing "empty string is considered substring"
    (is (= (string/substring? "" "foo")
           true)))
  (testing "case-sensitive non-substrings"
    (is (= (string/substring? "FO" "foo")
           (string/substring? "FO" "foo" :case-sensitive? true)
           false)))
  (testing "non substrings"
    (is (= (string/substring? "bar" "foo") false))))

(deftest error-json->str-test
  (testing "Renders JSON as k: v with capitalized k"
    (is (= (string/error-json->str {:a-key "a value"}) "A-key: a value")))

  (testing "Joins when v is a list"
    (is (= (string/error-json->str {:a-key ["a value"]}) "A-key: a value"))))

(deftest humanize-number-test
  (testing "Adds commas to one million"
    (is (= (string/humanize-number 1000000) "1,000,000")))

  (testing "Does not add comma to one hunder"
    (is (= (string/humanize-number 100) "100"))))
