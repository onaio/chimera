(ns chimera.core)

#?(:clj
   (def any? (complement not-any?)))
(def not-nil? (complement nil?))
