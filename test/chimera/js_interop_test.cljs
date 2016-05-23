(ns chimera.js-interop-test
  (:require-macros [cljs.test :refer (is deftest)])
  (:require [cljs.test :as t]
            [chimera.js-interop :as i]))

(def json-blob
  "{\"hint\": \"entrer la date d'aujourd'hui\",
    \"label\": \"Date de l'entrevue\",
    \"name\": \"intrevue_date\",
    \"type\": \"date\"}")

(deftest json-converters
  (let [cljs-plain (i/json->cljs json-blob)
        cljs-keyed (i/json->js->cljs json-blob)
        js-plain (i/str->json json-blob)]
    (is (= (keys cljs-plain)) ["hint" "label" "name" "type"])
    (is (= (keys cljs-keyed)) [:hint :label :name :type])
    (is (= (vals cljs-plain) (vals cljs-keyed)))
    (is (= (keys cljs-keyed) (map keyword (keys cljs-plain))))
    (is (= cljs-plain (js->clj js-plain)))
    (is (= cljs-keyed (js->clj js-plain :keywordize-keys true)))))

;; Comparison of actual regexes not possible to do due to this bug
;; https://bugs.openjdk.java.net/browse/JDK-7163589
;; Either that or we can use a testing framework called expectations:
;; http://jayfields.com/expectations/
(deftest safe-regex
  (is (= (str (i/safe-regex "foo"))
         (str #"(?i)foo")))
  (is (= (re-find #"(?i)Foo" "foo")
         (re-find (i/safe-regex "Foo") "foo") "foo"))
  (is (= (re-find #"(?i)Fo\?o" "fo?o")
         (re-find (i/safe-regex "Fo?o") "fo?o") "fo?o"))
  (is (= (re-find #"(?i)Fo\)o" "fo)o")
         (re-find (i/safe-regex "Fo)o") "fo)o") "fo)o")))
