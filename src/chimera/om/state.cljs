(ns ona.chimera.om.state
  (:require [om.core :as om :include-macros true]))

(defn app-state->transact
  [app-state]
  (if (satisfies? om/ITransact app-state)
    om/transact! swap!))

(defn transact!
  ([app-state transact-fn]
   ((app-state->transact app-state) app-state transact-fn))
  ([app-state ks transact-fn]
   ((app-state->transact app-state) app-state update-in ks transact-fn)))

(defn merge-into-app-state!
  "Merges provided state into existing app-state, possibly after zooming into
   ks."
  ([app-state state-to-merge]
   (transact! app-state #(merge % state-to-merge)))
  ([app-state ks state-to-merge]
   (transact! app-state ks #(merge % state-to-merge))))
