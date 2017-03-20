(defn js-dir
      "Prefix with full JavaScript directory."
      [path]
      (str "resources/public/js/compiled/" path))

(defproject onaio/chimera "0.0.6"
  :description "Collection of useful Clojure(Script) functions."
  :dependencies [[clj-time "0.12.2"]
                 [com.cognitect/transit-cljs "0.8.239"]
                 [com.taoensso/tempura "1.0.0"]
                 ;; For CSV->XLSForm
                 [clojure-csv/clojure-csv "2.0.2"]
                 [dk.ative/docjure "1.11.0"]
                 [onelog "0.4.5"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293"
                  :exclusions [org.clojure/clojure]]
                 [org.clojure/core.async "0.2.395"]
                 [org.omcljs/om "0.9.0"]
                 [slingshot "0.12.2"]
                 ;; JS
                 [cljsjs/moment "2.10.6-4"]]
  :license "Apache 2"
  :url "https://github.com/onaio/chimera"
  :profiles {:dev {:dependencies [[midje "1.8.3"]]}}
  :plugins [[jonase/eastwood "0.2.1"]
            [lein-bikeshed-ona "0.2.1"]
            [lein-cljfmt "0.3.0"]
            [lein-cljsbuild "1.1.2"]
            [lein-environ "1.0.1"]
            [lein-kibit "0.1.2"]
            [lein-midje "3.1.3"]]
  :cljfmt {:file-pattern #"[^\.#]*\.clj[s]?$"}
  :eastwood {:exclude-linters [:constant-test]
             :add-linters [:unused-fn-args
                           :unused-locals
                           :unused-namespaces
                           :unused-private-vars]
             :namespaces [:source-paths]
             :exclude-namespaces [chimera.async]}
  :test-paths ["test"]
  :cljsbuild {
              :builds {:dev
                       {:compiler {:output-to ~(js-dir "chimera.js")
                                   :output-dir ~(js-dir "out")
                                   :optimizations :whitespace
                                   :pretty-print true
                                   :source-map ~(js-dir "chimera.js.map")}}
                       :test
                       {:source-paths ["src" "test"]
                        :notify-command ["phantomjs"
                                         "phantom/unit-test.js"
                                         "phantom/unit-test.html"
                                         "target/main-test.js"]
                        :compiler {:output-to "target/main-test.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}
                       :prod
                       {:source-paths ["src"]
                        :compiler {:output-to ~(js-dir "chimera.js")
                                   :output-dir ~(js-dir "out-prod")
                                   :optimizations :advanced
                                   :pretty-print false}
                        :jar true}}
              :test-commands {"unit-test"
                              ["phantomjs"
                               "phantom/unit-test.js"
                               "phantom/unit-test.html"
                               "target/main-test.js"]}}
  :global-vars {*warn-on-reflection* true})
