(ns chimera.core)

(def any? (complement not-any?))
(def not-nil? (complement nil?))
(def strict-map (comp doall map))
