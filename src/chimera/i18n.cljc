(ns chimera.i18n
  (:require [clojure.string :refer [blank?]]
            [taoensso.tempura :as tempura]))

(defn generate-filename
  [language-code styling-theme]
  (str "translations/" (name styling-theme) "/" (name language-code) ".clj"))

(defn dictionary
  "Generate a map with languagecodes as keys, file names as values"
  [languages]
  (reduce (fn [m {:keys [iso-code styling-theme]}]
            (assoc m
                   iso-code {:__load-resource
                             (generate-filename iso-code styling-theme)}))
          {}
          languages))

(defn- merge-keywords
  [keywords]
  (apply keyword (keep #(some-> % str (subs 1)) keywords)))

(defn tr
  "Call the library translation function. If the final argument is a vector
   assume it is a resource-arg and the rest make up a keyword resource ID,
   otherwise assume all args make up a keyword resource ID."
  [opts language-codes args]
  (let [last-arg (last args)
        has-args? (vector? last-arg) ; true if last element is resource-args
        resource-ids [(merge-keywords (if has-args? (butlast args) args))]]
    (tempura/tr opts language-codes resource-ids (when has-args? last-arg))))
