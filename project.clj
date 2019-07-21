(defproject arrival-test-task "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.520"]
                 [reagent "0.8.1"]
                 [re-frame "0.10.6"]
                 [garden "1.3.9"]
                 [ns-tracker "0.4.0"]
                 [compojure "1.6.1"]
                 [yogthos/config "1.1.2"]
                 [ring "1.7.1"]
                 ;;
                 ;; [cheshire "5.8.1"]
                 ;; [ring-middleware-format "0.7.4"]
                 ;; [com.datomic/client-pro "0.8.28"]
                 [com.datomic/datomic-pro "0.9.5927" :exclusions [com.google.guava/guava]]
                 [clj-commons/secretary "1.2.4"]

                 [day8.re-frame/test "0.1.5"]
                 [lein-doo "0.1.11"]
                 [devcards "0.2.6"]
                 ;;
                 ]

  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :username ~(System/getenv "DATOMIC_USERNAME")
                                   :password ~(System/getenv "DATOMIC_PASSWORD")}}

  :main arrival-test-task.server

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-garden "0.2.8"]
            [lein-doo "0.1.11"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "resources/public/css"]

  :figwheel {:css-dirs ["resources/public/css"]
             ;; :ring-handler arrival_test_task.handler/dev-handler
             }

  :garden {:builds [{:id           "screen"
                     :source-paths ["src/clj"]
                     :stylesheet   arrival_test_task.css/screen
                     :compiler     {:output-to     "resources/public/css/screen.css"
                                    :pretty-print? true}}]}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.10"]
                                  [re-frisk "0.5.4.1"]]

                   :plugins      [[lein-figwheel "0.5.18"]]

                  ; :source-paths ["test"]
                  ; :test-path "test"

                   :extra-paths ["test"] ;; ["ui/srcs" "ui/test" "backend/test"]
                  ;  :extra-deps {com.cognitect/test-runner
                  ;               {:git/url "https://github.com/cognitect-labs/test-runner.git"
                  ;                :sha "3cb0a9daf1cb746259dc8309b218f9211ad3b33b"}
                                ; faker {:mvn/version "0.2.2"}
                                ; re-frame {:mvn/version "0.10.5"}
                                ; org.clojure/tools.cli {:mvn/version "0.4.1"}

                                ; nrepl/nrepl {:mvn/version "0.6.0"}
                                ; refactor-nrepl {:mvn/version "2.4.0"}
                                ; cider/cider-nrepl {:mvn/version "0.22.0-beta4"}
                  ;              }
                   ;;
                   }
             :prod {}
             :uberjar {:source-paths ["env/prod/clj"]
                       :omit-source  true
                       :main         arrival-test-task.server
                       :aot          [arrival-test-task.server]
                       :uberjar-name "arrival_test_task.jar"
                       :prep-tasks   ["compile" ["cljsbuild" "once" "min"]["garden" "once"]]}}

  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src/cljs"]
                        :figwheel     {:on-jsload "arrival_test_task.core/mount-root"}
                        :compiler     {:main                 arrival_test_task.core
                                       :output-to            "resources/public/js/compiled/app.js"
                                       :output-dir           "resources/public/js/compiled/out"
                                       :asset-path           "js/compiled/out"
                                       :source-map-timestamp true
                                       :preloads             [devtools.preload
                                                              re-frisk.preload]
                                       :external-config      {:devtools/config {:features-to-install :all}}}}

                       {:id           "min"
                        :source-paths ["src/cljs"]
                        :jar true
                        :compiler     {:main            arrival_test_task.core
                                       :output-to       "resources/public/js/compiled/app.js"
                                       :optimizations   :advanced
                                       :closure-defines {goog.DEBUG false}
                                       :pretty-print    false}}

                       {:id           "test"
                        :source-paths ["src/cljs" "test"] ;; ["src/cljs" "test/ui"];; ["src" "test"]
                        :compiler     {:main          ui.test-runner ;; runners.doo
                                       ;; karma - browsers
                                       :optimizations :whitespace
                                       :output-dir    "resources/public/js/compiled/test"
                                       :output-to     "resources/public/js/compiled/test.js"}}

                       {:id           "devcards-test"
                        :source-paths ["src/cljs" "test"] ;; ["src" "test"]
                        :figwheel     {:devcards true}
                        :compiler     {:main                 dev-cards.test-runner
                                       :optimizations        :none
                                       :asset-path           "js/compiled/devcards-test"
                                       :output-dir           "resources/public/js/compiled/devcards-test"
                                       :output-to            "resources/public/js/compiled/devcards-test.js"
                                       :source-map-timestamp true}}
                       ;;
                       ]})
