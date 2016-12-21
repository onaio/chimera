(ns chimera.metrics
  (:require [chimera.seq :refer [remove-nil]]
            [clojure.string :refer [join]]))

;;; Metrics library for wiring event handles to an external service that
;;; receives metric events via JavaScript calls

(defn call-ga
  "Get google analytics `ga` object and call with `args`. If `ga` does not
   exist do nothing."
  [& args]
  (let [final-args (remove-nil args)]
    #?(:clj (format "ga('%s');" (join "', '" final-args))
       :cljs (when-let [g (aget js/window "ga")]
               (apply g final-args)))))

(defn- call-ga-send-event
  [& args]
  (apply call-ga (concat ["send" "event"] args)))

(defprotocol AnalyticsEvent
  "Create google analytics events."
  (call-analytics-event [x event-label event-value]))

(defrecord GaEvent [event-category event-action]
  AnalyticsEvent
  (call-analytics-event
    [x event-label event-value]
    (call-ga-send-event event-category
                        event-action
                        event-label
                        event-value)))

(defn send-event
  "Generic event handler. Add other external metric services here."
  [event-category event-action & {:keys [event-label event-value]}]
  (call-analytics-event (GaEvent. event-category event-action)
                        event-label
                        event-value))
