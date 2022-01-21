(defn js-dir
  "Prefix with full JavaScript directory."
  [path]
  (str "resources/public/js/compiled/" path))

(defproject onaio/chimera "0.0.14"
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
                 [org.omcljs/om "1.0.0-beta2"]
                 ;; JS
                 [slingshot "0.12.2"]
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
            [lein-midje "3.2.1"]
            [lein-doo "0.1.11"]]
  :cljfmt {:file-pattern #"[^\.#]*\.clj[s]?$"}
  :eastwood {:exclude-linters [:constant-test]
             :add-linters [:unused-fn-args
                           :unused-locals
                           :unused-namespaces
                           :unused-private-vars]
             :namespaces [:source-paths]
             :exclude-namespaces [chimera.async]}
  :test-paths ["test"]
  :cljsbuild {:builds {:dev
                       {:compiler {:output-to ~(js-dir "chimera.js")
                                   :output-dir ~(js-dir "out")
                                   :optimizations :whitespace
                                   :pretty-print true
                                   :source-map ~(js-dir "chimera.js.map")}}
                       :test
                       {:source-paths ["src" "test"]
                        :compiler {:output-to "test-output/test-file.js"
                                   :main test-runner
                                   :optimizations :none}}
                       :prod
                       {:source-paths ["src"]
                        :compiler {:output-to ~(js-dir "chimera.js")
                                   :output-dir ~(js-dir "out-prod")
                                   :optimizations :advanced
                                   :pretty-print false}
                        :jar true}}}
  :global-vars {*warn-on-reflection* true})
