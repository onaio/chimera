(ns chimera.metrics-test
  (:require-macros [cljs.test :refer (is deftest)])
  (:require [cljs.test :as t]
            [chimera.metrics :as metrics]))

(deftest test-call-gtag
  (let [event "click_event"
        name "dataview-toggle"
        args {:value "on"}]
    (set! (.-gtag js/window) (fn [event name args]
                               (is (= event "click_event"))
                               (is (= name "dataview-toggle"))
                               (is (= (js->clj args) {"value" "on"}))))
    (metrics/call-gtag event name args)))