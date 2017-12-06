(ns chimera.seq-test
  #?(:cljs (:require-macros [cljs.test :refer [is deftest testing]]))
  (:require [chimera.seq :refer [filter-collection
                                 filter-out-collection
                                 flatten-map
                                 full-stringify-keys
                                 in?
                                 indexed
                                 ordered-diff
                                 remove-falsey-values
                                 remove-nil
                                 toggle
                                 add-element
                                 remove-element]]
            #?(:clj [clojure.test :as t :refer [is deftest testing]]
               :cljs [cljs.test :as t])))

(deftest in?-test
  (testing "finds element in list"
    (is (in? [:a] :a))))

(deftest remove-falsey-values-test
  (testing "remove-falsey-values removes keys that are nil or blank"
    (let [original-map {:worthy "Worthy" :is-blank "" :is-nil nil}
          transformed-map {:worthy "Worthy"}]
      (is (= (remove-falsey-values original-map) transformed-map)))))

(deftest different-test
  (let [project-list-1 [{:title "Project 1"
                         :projectid 1
                         :users [{:user "user 1"} {:user "user 2"}]}
                        {:title "Project 2"
                         :projectid 2
                         :users [{:user "user 1"} {:user "user 2"}]}]
        project-list-2 [{:title "Project 1"
                         :projectid 1
                         :users [{:user "user 1"} {:user "user 2"}]}
                        {:title "Project 2"
                         :projectid 2
                         :users [{:user "user 1"} {:user "user 2"}
                                 {:user "user 3"}]}]]

    (testing "different? is true if list of maps are different"
      (is (not= project-list-1 project-list-2)))

    (testing "different? is false if list of maps are same"
      (is (= project-list-1 project-list-1)))))

(deftest filter-collection-test
  (let [list [{:a 1} {:a 1} {:b 2} {:b 2}]]
    (testing "returns empty list if passed empty list"
      (is (= (filter-collection :some-key :filter-cond []) [])))

    (testing "returns full list if cond is nil or false"
      (is (= (filter-collection :c nil list)) list))

    (testing "returns empty list if no key matches and cond is not nil"
      (is (= (filter-collection :c 1 list)) []))

    (testing "returns empty list if key matches none of cond"
      (is (= (filter-collection :b 1 list)) []))

    (testing "returns partial list if key matches some of cond"
      (is (= (filter-collection :b 2 list)) (drop 2 list)))))

(deftest filter-out-collection-test
  (let [list [{:a 1} {:a 1} {:b 2} {:b 2}]]
    (testing "returns empty list if passed empty list"
      (is (= (filter-out-collection :some-key :filter-cond []) [])))

    (testing "returns full list if cond is nil or false"
      (is (= (filter-out-collection :c nil list)) list))

    (testing "returns full list if no key matches and cond is not nil"
      (is (= (filter-out-collection :c 1 list)) list))

    (testing "returns full list if key matches none of cond"
      (is (= (filter-out-collection :b 1 list)) list))

    (testing "returns partial list if key matches some of cond"
      (is (= (filter-out-collection :b 2 list)) (take 2 list)))))

(deftest flatten-map-test
  (testing "flattens first order nesting correctly"
    (is (= (flatten-map {:key "value"})
           {"key" "value"})))

  (testing "flattens higher order nesting correctly"
    (is (= (flatten-map {:grandparent {:parent "child"}})
           {"grandparent/parent" "child"}))))

(let [l1 [:a :b :c :d :e :f]
      l2 [:a :c]
      l3 [nil true true false true nil]]
  (deftest ordered-diff-test
    (testing "ordered_diff creates is seq 1 - seq 2, in order"
      (is (= (ordered-diff l1 l2) [:b :d :e :f]))
      (is (= (ordered-diff l2 l1) []))
      (is (= (ordered-diff l1 []) l1))
      (is (= (ordered-diff [] l1) []))
      (is (= (ordered-diff l1 nil) l1))
      (is (= (ordered-diff nil nil) ()))
      (is (= (ordered-diff l1 nil) l1))
      (is (= (ordered-diff l1 l3) l1))))

  (deftest toggle-test
    (testing "toggle toggles the presence of elements in a list"
      (is (= (toggle l1 nil) (conj l1 nil)))
      (is (= (toggle l2 :a) [:c]))
      (is (= (toggle l2 :b) [:a :c :b]))
      (is (= (toggle [] :a) [:a]))
      (is (= (toggle [:a] :a) []))
      (is (= (toggle [nil] nil) []))
      (is (= (toggle [] nil) [nil]))
      (is (= (toggle l3 nil) (remove-nil l3)))))

  (deftest remove-nil-test
    (testing "remove-nil takes nil out of lists"
      (is (= (remove-nil l3) [true true false true]))
      (is (= (remove-nil l1) l1))
      (is (= (remove-nil []) []))
      (is (= (remove-nil nil) ()))))

  (deftest indexed-test
    (testing "indexed produces an indexed list"
      (is (= (indexed [:a :b :c]) [[0 :a] [1 :b] [2 :c]]))
      (is (= (indexed []) []))
      (is (= (indexed nil) ())))))

(deftest full-stringify-keys-test
  (testing "normal keys are converted to strings"
    (is (= (full-stringify-keys {:a 1})
           {"a" 1})))

  (testing "dotted keys are converted to strings"
    (is (= (full-stringify-keys {:a.b 1})
           {"a.b" 1})))

  (testing "namespaced keys are converted to strings"
    (is (= (full-stringify-keys {:a/b.c 1})
           {"a/b.c" 1}))))

(deftest adding-and-removing-elements-from-a-vector
  (testing "add element to a vector"
    (is (= (sort (add-element [1 2] 3))
           [1 2 3] ))
    (is (= (sort (remove-element [1 2 3 4] 4))
           [1 2 3]))))
