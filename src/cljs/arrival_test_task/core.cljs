(ns arrival-test-task.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [arrival-test-task.events :as events]
   [arrival-test-task.views :as views]
   [arrival-test-task.config :as config]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (rf/clear-subscription-cache!)
  (reagent/render [views/main-panel] (.getElementById js/document "app")))

(defn ^:export init []
  (rf/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
