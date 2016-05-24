(ns chimera.compositions)

(def any? (complement not-any?))
(def not-nil? (complement nil?))
