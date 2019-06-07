(ns chimera.metrics
  (:require [chimera.seq :refer [remove-nil]]
            [clojure.string :refer [join]]))

;;; Metrics library for wiring event handlers to external services that
;;; receive metric events via JavaScript calls.

(defn call-ga
  "In CLJ, return a string that is a valid JS function call to `ga` passing
   it the non-nil `args`.
   In CLJS, get google analytics `ga` object and call with the non-nil `args`.
   If `ga` does not exist do nothing."
  [& args]
  (let [final-args (remove-nil args)]
    #?(:clj (format "ga('%s');" (join "', '" final-args))
       :cljs (when-let [g (aget js/window "ga")]
              (try
               (apply g final-args)
               (catch js/TypeError e
                (apply (.-ga js/window) nil final-args)))))))

(defprotocol AnalyticsEvent
  "Generic protocol for analytics events."
  (call-analytics-event
    [x event-label event-value]
    "Call the event appropriately for the target."))

(defrecord GaEvent [event-category event-action]
  AnalyticsEvent
  (call-analytics-event
    [x event-label event-value]
    (call-ga "send"
             "event"
             event-category
             event-action
             event-label
             event-value)))

(defn send-event
  "Generic event handler. Add other external metric services here."
  [event-category event-action & {:keys [event-label event-value]}]
  (call-analytics-event (GaEvent. event-category event-action)
                        event-label
                        event-value))
