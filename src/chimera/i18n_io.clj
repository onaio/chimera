(ns chimera.i18n-io
  (:refer-clojure :exclude [replace])
  (:require [chimera.i18n :refer [dictionary generate-filename]]
            [chimera.seq :refer [assoc-in-multi
                                 deep-merge
                                 filter-first
                                 flatten-map]]
            [clojure.java.io :refer [as-file file make-parents writer]]
            [clojure.pprint :refer [pprint pprint-newline]]
            [clojure.string :refer [blank?
                                    join
                                    replace
                                    split
                                    split-lines
                                    starts-with?
                                    trim]]
            [clojure.walk :refer [walk]]
            [onelog.core :as log]))

(defn- get-resource-path
  [path]
  (str "resource/" path))

(defn get-language-name
  [language-code available-languages]
  (:name (filter-first #(= (:iso-code %) language-code) available-languages)))

(defn get-translation-path
  [language-code]
  (get-resource-path (generate-filename language-code)))

;; WRITING TRANSLATION MAP TO FILE

(defn write-translation-file
  "Write string to an outpupt file based on filename."
  [filename file-format ^String contents]
  (let [path (format "target/translations/%2$s/%1$s.%2$s"
                     filename (name file-format))]
    (make-parents path)
    (with-open [^java.io.Writer out-file (writer path)]
      (.write out-file contents)
      (log/info (str "Translation file written to " path)))))

(defn generate-translation-maps
  [languages]
  (let [code->map
        (into {} (for [[code {filepath :__load-resource}] languages]
                   [code (->> filepath
                              get-resource-path
                              load-file
                              flatten-map)]))]
    (for [[code translations] code->map]
      (into {} (for [[k v] translations] [k {code v}])))))

(defn inner-transformer
  [[translation-key translation-value]]
  (if (map? translation-value)
    [translation-key (walk inner-transformer
                           identity
                           translation-value)]
    [translation-key nil]))

(defn wrap-english-translation-values
  [language-map]
  (walk inner-transformer identity language-map))

(defn update-translation-file
  [target-language-code available-languages]
  (let [en-translation-map (load-file (get-translation-path :en))
        modified-value-translation-map (wrap-english-translation-values
                                        en-translation-map)
        target-language-translation-file-path (get-translation-path
                                               target-language-code)
        target-language-translation-map
        (load-file target-language-translation-file-path)
        updated-translation-map
        (deep-merge modified-value-translation-map
                    target-language-translation-map)]
    (with-open [out-file (writer target-language-translation-file-path)]
      (pprint updated-translation-map out-file)
      (log/start! "/dev/stdout")
      (log/info
       (get-language-name target-language-code available-languages)
       " translation file updated."))))

(defn- quote-string
  [string-to-quote]
  (str "\"" string-to-quote "\""))

(defn- process-string
  [string-to-process]
  (when string-to-process
    (->> string-to-process
         split-lines
         (map quote-string)
         (join " "))))

(defn generate-po-file-entry
  [canonical-translation-map]
  (fn [[fully-qualified-i18n-key entry]]
    (let [msgid (-> fully-qualified-i18n-key
                    canonical-translation-map
                    vals
                    first)
          msgstr (-> entry vals first)]
      (when (and msgid msgstr)
        (->> ["msgctxt" (quote-string fully-qualified-i18n-key)
              "msgid" (process-string msgid)
              "msgstr" (process-string msgstr)]
             (partition 2)
             (map #(join " " %))
             (join "\n"))))))

(defn clj->gettext
  [canonical-translation-map destination-translation-map]
  (->> destination-translation-map
       (map (generate-po-file-entry canonical-translation-map))
       (remove nil?)
       (join "\n\n")))

(defn generate-translation-po-file
  [language-code available-languages]
  (let [english-translation-map (-> (dictionary available-languages)
                                    (select-keys [:en])
                                    (generate-translation-maps)
                                    (first))]
    (log/start! "/dev/stdout")
    (->> {language-code (generate-filename language-code)}
         (generate-translation-maps)
         (first)
         (clj->gettext english-translation-map)
         (write-translation-file (name language-code)
                                 :po))))

(def is-po-comment? #(starts-with? % "#"))

(defn- process-po-string-for-importing
  [string-to-process]
  (when (string? string-to-process)
    (-> string-to-process
        (replace #"\"| $|^ |^\n" "")
        (replace #"\n" " ")
        (replace #"^ " "")
        (replace #"  " " "))))

(defn- generate-i18n-key-vector-value-pairs
  [[raw-key _ raw-i18n-string-value]]
  (let [i18n-string-value
        (process-po-string-for-importing raw-i18n-string-value)]
    (when-not (blank? i18n-string-value)
      [(->> (-> raw-key
                process-po-string-for-importing
                (split #"/"))
            (map keyword)
            vec)
       i18n-string-value])))

(defn split-paragraphs
  [text]
  (split text #"\n\n"))

(defn gettext->clj
  [po-formatted-translations]
  (->> po-formatted-translations
       split-lines
       (remove is-po-comment?)
       (join "\n")
       split-paragraphs
       (map #(replace % #"^msgctxt" ""))
       (map #(split % #"\nmsgid|\nmsgstr"))
       (map generate-i18n-key-vector-value-pairs)
       (remove nil?)
       (map #(apply assoc-in {} %))
       (apply deep-merge)))

(defn load-translation-po-file
  [file-path target-language-code]
  (let [file-contents (slurp file-path)
        target-language-translation-file-path
        (get-translation-path target-language-code)
        target-language-translation-map
        (load-file target-language-translation-file-path)
        updated-translations-map
        (gettext->clj file-contents)]
    (with-open [out-file (writer target-language-translation-file-path)]
      (pprint (deep-merge target-language-translation-map
                          updated-translations-map)
              out-file)
      (log/start! "/dev/stdout")
      (log/info (str "Translation file written to "
                     target-language-translation-file-path)))))
