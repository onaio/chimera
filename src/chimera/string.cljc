(ns chimera.string
  (:require [chimera.core :refer [not-nil?]]
            [clojure.string :refer [lower-case]]
            #?(:cljs [goog.string :refer [format]])))

;;; Validation regexes
(def email-regex #"(?i)^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,6}$")
(def twitter-username-regex #"^[A-Za-z0-9_]*$")

;;; Truncation variables
(def truncate-if-longer-than 50)
(def ellipsis-start 36)
(def ellipsis-stop-from-end 12)

;;; String types
(def chars->entities {\< "&lt;" \> "&gt;" \& "&amp;" \" "&quot;" \' "&#39;"})
(def vowel? (set "aeiou"))

(defn parenthesize-suffix
  [prefix suffix]
  (str prefix " (" suffix ")"))

(defn truncate-with-ellipsis
  "Shorten a string to a certain length with middle ellipsis."
  [string]
  (if (> (count string) truncate-if-longer-than)
    (let [end-start (- (count string) ellipsis-stop-from-end)]
      (str
       (subs string 0 ellipsis-start)
       "..."
       (subs string end-start)))
    string))

(defn first-cap
  "Return the first character of a string capitalized."
  [string]
  (-> string first str string/capitalize))

(defn ^Boolean is-email?
  "True if string is an email address."
  [string]
  #?(:clj  (re-matches email-regex string)
     :cljs (first (.match string email-regex))))

(defn ^Boolean is-twitter-username?
  "True if string is a valid twitter username"
  [string]
  #?(:clj  (re-matches twitter-username-regex string)
     :cljs (first (.match string twitter-username-regex))))

(defn postfix-paren-count
  "Wrap the count of a collection in parens and postfix."
  [prefix collection]
  (parenthesize-suffix prefix (count collection)))

(defn ^Boolean ends-with?
  "True if string ends with the passed suffix."
  [string suffix]
  (let [offset (- (count string) (count suffix))]
    (and (>= offset 0)
         (= suffix (subs string offset)))))

(defn map->js-string-map
  "Serialize a Clojure map to JavaScript string map."
  [m]
  (string/join
   (flatten
    ["{" (interpose "," (for [[k v] m] ["\"" (name k) "\":\"" v "\""])) "}"])))

(defn begins-with-vowel?
  "True if the first letter of string is a vowel."
  [string]
  ;; call first again to convert string to char
  (-> string first string/lower-case first vowel?))

(defn false-str->false
  "If arg is a string and equal to 'false' in lower case return false,
   otherwise return the string."
  [x]
  (if (= (and (string? x) (string/lower-case x)) "false") false x))

(defn error-json->str
  "Render a JSON error as a string for the user."
  [json]
  (string/join "<br/>"
               (for [[k v] json :let [key (-> k name string/capitalize)
                                      value (cond->> v
                                              (vector? v)
                                              (string/join " "))]]
                 (format "%s: %s" key value))))

(defn unescape
  "Build an unescaped string."
  [s]
  (if (string? s)
    (reduce (fn [r [k v]] (string/replace r (re-pattern v) (str k)))
            s chars->entities)
    s))

(defn is-null?
  "Checks if a variable is null"
  [s]
  (= "null" s))

(def is-not-null?
  (complement is-null?))

(defn safe-lower-case [str] (when (string? str) (string/lower-case str)))

(defn ^Boolean substring?
  "True if substring is a substring of string"
  [substring string & {:keys [case-sensitive?] :or {case-sensitive? true}}]
  (if (or (empty? string)
          #?(:cljs (is-null? string)))
    false
    (not-nil? (re-find (re-pattern
                        (str (when-not case-sensitive? "(?i)") substring))
                       string))))
