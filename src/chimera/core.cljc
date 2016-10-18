(ns chimera.core
  #?(:cljs (:refer-clojure :exclude [any?])))

(def any? (complement not-any?))
(def not-nil? (complement nil?))
(def strict-map (comp doall map))
