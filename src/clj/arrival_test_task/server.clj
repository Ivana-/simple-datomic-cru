(ns arrival-test-task.server
  (:require [arrival-test-task.handler :as handler]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]))

(defonce server (atom nil))

(defn start-server! []
  (let [port (Integer/parseInt (or (env :port) "3000"))]
    (reset! server (cond-> {:main (run-jetty handler/dev-handler {:port port :join? false})}
                     (env :start-test-server?)
                     (assoc :test (run-jetty handler/test-handler {:port (inc port) :join? false}))))
    (println (str "Main server is running on port " port))
    (when (env :start-test-server?)
      (println (str "Test server is running on port " (inc port))))))

(defn stop-server! [] (doseq [x (vals @server)] (.stop x)))


(comment
  (start-server!)
  (identity @server)

  (do
    (stop-server!)
    (start-server!))
  ;;
  )