(ns chimera.core-test
  #?(:cljs (:require-macros [cljs.test :refer [is deftest testing]]))
  (:require [chimera.core :as core]
            [clojure.string :refer [blank?]]
            #?(:clj [clojure.test :as t :refer [is deftest testing]]
               :cljs [cljs.test :as t])))

(deftest compositions-api
  (testing "any? calls complement on not-any?"
    (is (= true (core/any? false? '(true true false true)))))
  (testing "not-nil? calls complement on nil?"
    (is (= true (core/not-nil? [])))))
