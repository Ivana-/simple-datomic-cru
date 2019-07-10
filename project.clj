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
                 [ring-middleware-format "0.7.4"]
                 ;; [com.datomic/client-pro "0.8.28"]
                 [com.datomic/datomic-pro "0.9.5927" :exclusions [com.google.guava/guava]]
                 [clj-commons/secretary "1.2.4"]
                 ;;
                 ]

  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :username ~(System/getenv "DATOMIC_USERNAME")
                                   :password ~(System/getenv "DATOMIC_PASSWORD")}}

  :main arrival-test-task.server

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-garden "0.2.8"]]

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

  :profiles
  {:dev {:dependencies [[binaryage/devtools "0.9.10"]
                        [re-frisk "0.5.4.1"]]

         :plugins      [[lein-figwheel "0.5.18"]]}
   :prod {}
   :uberjar {:source-paths ["env/prod/clj"]
             :omit-source  true
             :main         arrival-test-task.server
             :aot          [arrival-test-task.server]
             :uberjar-name "arrival_test_task.jar"
             :prep-tasks   ["compile" ["cljsbuild" "once" "min"]["garden" "once"]]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
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
                    :pretty-print    false}}]})
