(ns arrival-test-task.core
  (:require [arrival-test-task.config :as config]
            [arrival-test-task.datomic :as datomic]
            [arrival-test-task.server :as server])
  (:gen-class))

(defn -main [& args]
  (config/load-config!)
  (datomic/start-datomic!)
  (server/start-server!))

(comment
  (-main)
  ;;
  )
