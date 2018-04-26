(ns chimera.string
  (:require [chimera.core :refer [not-nil?]]
            [clojure.string :as string]
            [#?(:clj clojure.pprint
                :cljs cljs.pprint) :refer [cl-format]]))

;;; Validation regexes
(def email-regex #"(?i)^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,6}$")
(def twitter-username-regex #"^[A-Za-z0-9_]*$")
(def domain-name-regex #"([a-z0-9|-]+\.)*[a-z0-9|-]+\.[a-z]+")

;;; Truncation variables
(def truncate-if-longer-than 50)
(def ellipsis-start 36)
(def ellipsis-stop-from-end 12)

(defn str-is-true?
  "Return true if string value is true else false"
  [value]
  (and (not-nil? value)
       (= (string/lower-case value) "true")))

;;; String types
(def chars->entities {\< "&lt;" \> "&gt;" \& "&amp;" \" "&quot;" \' "&#39;"})
(def vowel? (set "aeiou"))

(defn parenthesize-suffix
  [prefix suffix]
  (str prefix " (" suffix ")"))

(defn truncate-with-ellipsis
  "Shorten a string to a certain length with middle ellipsis."
  [s]
  (if (> (count s) truncate-if-longer-than)
    (let [end-start (- (count s) ellipsis-stop-from-end)]
      (str (subs s 0 ellipsis-start)
           "..."
           (subs s end-start)))
    s))

(defn first-cap
  "Return the first character of a string capitalized."
  [s]
  (-> s first str string/capitalize))

(defn ^Boolean is-email?
  "True if `s` is an email address."
  [s]
  #?(:clj  (re-matches email-regex s)
     :cljs (first (.match s email-regex))))

(defn ^Boolean is-twitter-username?
  "True if `s` is a valid twitter username"
  [s]
  #?(:clj  (re-matches twitter-username-regex s)
     :cljs (first (.match s twitter-username-regex))))

(defn postfix-paren-count
  "Wrap the count of a collection in parens and postfix."
  [prefix collection]
  (parenthesize-suffix prefix (count collection)))

(defn ^Boolean ends-with?
  "True if `s` ends with the passed suffix."
  [s suffix]
  (let [offset (- (count s) (count suffix))]
    (and (>= offset 0)
         (= suffix (subs s offset)))))

(defn map->js-string-map
  "Serialize a Clojure map to JavaScript string map."
  [m]
  (string/join
   (flatten
    ["{" (interpose "," (for [[k v] m] ["\"" (name k) "\":\"" v "\""])) "}"])))

(defn begins-with-vowel?
  "True if the first letter of `s` is a vowel."
  [s]
  ;; call first again to convert string to char
  (-> s first string/lower-case first vowel?))

(defn false-str->false
  "If `s` is a string and equal to 'false' in lower case return false,
   otherwise return `s`."
  [s]
  (if (= (and (string? s) (string/lower-case s)) "false") false s))

(defn error-json->str
  "Render a JSON error as a string for the user."
  [json]
  (string/join "<br/>"
               (for [[k v] json :let [key (-> k name string/capitalize)
                                      value (cond->> v
                                              (vector? v)
                                              (string/join " "))]]
                 (str key ": " value))))

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
  "True if substring is a substring of `s`"
  [substring s & {:keys [case-sensitive?] :or {case-sensitive? true}}]
  (if (or (empty? s) #?(:cljs (is-null? s)))
    false
    (not-nil? (re-find (re-pattern
                        (str (when-not case-sensitive? "(?i)") substring))
                       s))))

(defn humanize-number
  "Insert a comma into numbers in thousands and higher"
  [n]
  (cl-format nil "~:d" n))

(defn escape-for-type
  "Escape for types. Replaces characters in the string with this base on
  chars->entities map."
  [x]
  (cond (nil? x) "null"
        (string? x) (string/escape x chars->entities)
        :else x))
