(ns chimera.js-interop
  (:require [cognitect.transit :as transit]
            [goog.string :as gstring]))

(defn json->cljs
  "Convert json string to cljs object using transit.
   Fast, but doesn't preserve keywords."
  [s]
  (transit/read (transit/reader :json) s))

(defn str->json
  "Convert json to js using JSON.parse.
   If error occurs, return the string unmodified."
  [string]
  (try (.parse js/JSON string)
       (catch js/Error _ string)))

(defn json->js->cljs
  "Convert json string to cljs via js.
   Slow method, but preserves keywords, and appropriate for small json."
  [s]
  (js->clj (str->json s) :keywordize-keys true))

(defn format
  "Formats a string using goog.string.format, so we can use format in cljx."
  [fmt & args]
  (apply gstring/format fmt args))

(defn safe-regex
  "Create a safe (escaped) js regex out of a string.
   By default, creates regex with ignore case option."
  [query-string & {:keys [ignore-case?] :or {ignore-case? true}}]
  (re-pattern (str (when ignore-case? "(?i)")
                   (gstring/regExpEscape query-string))))
