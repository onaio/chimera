(ns chimera.i18n-io-test
  (:require [midje.sweet :refer :all]
            [chimera.i18n-io :refer [clj->gettext gettext->clj]]))

(def original-text "This is the English original text")
(def translated-text "Hili ni lugha lingine")
(def fully-qualified-i18n-key "deeply/nested/translation/key")

(def canonical-translation-map
  {fully-qualified-i18n-key {:en original-text}})

(def destination-translation-map
  {fully-qualified-i18n-key {:test-lang translated-text}})

(defn construct-po-entry
  [msgctxt msgid msgstr]
  (str "msgctxt \"" msgctxt "\"\n"
       "msgid \"" msgid "\"\n"
       "msgstr \"" msgstr "\""))

(def updated-po-file-contents
  (construct-po-entry fully-qualified-i18n-key
                      original-text
                      translated-text))

(def updated-translation-map
  {:deeply
   {:nested
    {:translation
     {:key translated-text}}}})

(facts "about gettext utilities"
       (fact "clj->gettext transforms translation map to PO file format"
             (clj->gettext canonical-translation-map
                           destination-translation-map)
             => updated-po-file-contents)
       (fact "gettext->clj merges PO file contents into existing map"
             (gettext->clj updated-po-file-contents)
             => updated-translation-map))
