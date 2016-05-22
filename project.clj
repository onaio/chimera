(defn js-dir
      "Prefix with full JavaScript directory."
      [path]
      (str "resources/public/js/" path))

(defproject onaio/chimera "0.0.1-SNAPSHOT"
  :description "Collection of useful Clojure functions."
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.51"
                  :exclusions [org.clojure/clojure]]
                 [com.cognitect/transit-cljs "0.8.237"]]
  :license "Apache 2"
  :url "https://github.com/onaio/chimera"
  :plugins [[jonase/eastwood "0.2.1"]
            [lein-bikeshed-ona "0.2.1"]
            [lein-cljfmt "0.3.0"]
            [lein-cljsbuild "1.1.1"]
            [lein-environ "1.0.1"]
            [lein-kibit "0.1.2"]]
  :cljfmt {:file-pattern #"[^\.#]*\.clj[s]?$"}
  :eastwood {:exclude-linters [:constant-test]
             :add-linters [:unused-fn-args :unused-locals :unused-namespaces
                           :unused-private-vars]
             :namespaces [:source-paths]}
  :test-paths ["tests/clj" "target/generated/tests/clj"]
  :cljsbuild {
              :builds {:dev
                       {:compiler {:output-to ~(js-dir "lib/main.js")
                                   :output-dir ~(js-dir "lib/out")
                                   :optimizations :whitespace
                                   :pretty-print true
                                   :source-map ~(js-dir "lib/main.js.map")}}
                       :test
                       {:source-paths ["src"]
                        :notify-command ["phantomjs"
                                         "phantom/unit-test.js"
                                         "phantom/unit-test.html"
                                         "target/main-test.js"]
                        :compiler {:output-to "target/main-test.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}
                       :prod
                       {:source-paths ["src"]
                        :compiler {:output-to ~(js-dir "lib/chimera.js")
                                   :output-dir ~(js-dir "lib/out-prod")
                                   :optimizations :advanced
                                   :pretty-print false}
                        :jar true}}
              :test-commands {"unit-test"
                              ["phantomjs"
                               "phantom/unit-test.js"
                               "phantom/unit-test.html"
                               "target/main-test.js"]}})
