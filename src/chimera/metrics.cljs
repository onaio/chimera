(ns chimera.metrics)

(defn call-gtag
  "Get google analytics `gtag` object and call with the non-nil `args`.
   If `gtag` does not exist do nothing."
  [event name args]
  (if-let [gtag (.-gtag js/window)]
    (.call gtag event name (clj->js args))
    (js/console.warn "gtag object is not defined")))

(defn call-analytics-event
  [event-category event-action event-label event-value]
  (call-gtag "event"
             "button_click"
             (merge
              {"event_category" (clj->js event-category)
               "event_action" (clj->js event-action)}
              (when event-value
                {"event_value" (clj->js event-value)})
              (when event-label
                {"event_value" (clj->js event-label)}))))

(defn send-event
  "Generic event handler. Add other external metric services here."
  [event-category event-action & {:keys [event-label event-value]}]
  (call-analytics-event event-category event-action
                        event-label
                        event-value))
