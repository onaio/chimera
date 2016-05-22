(ns chimera.seq-test
  #?(:cljs (:require-macros [cljs.test :refer [is deftest testing]]))
  (:require [chimera.seq :refer [in?]]
            #?(:clj [clojure.test :as t :refer [is deftest testing]]
               :cljs [cljs.test :as t])))

(deftest in?-test
  (testing "finds element in list"
    (is (in? [:a] :a))))
