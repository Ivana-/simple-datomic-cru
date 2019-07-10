(ns arrival-test-task.server
  (:require [arrival-test-task.handler :refer [handler]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defonce server (atom nil))

(defn -main [& args]
  (let [port (Integer/parseInt (or (env :port) "3000"))]
    (reset! server (run-jetty
                    ;; handler
                    arrival-test-task.handler/dev-handler
                    {:port port :join? false}))
    (println (str "Server is running on port " port))))

(comment
  (-main)
  (identity @server)

  (do
    (.stop @server)
    (-main)
    ;;
    )
  ;;
  )