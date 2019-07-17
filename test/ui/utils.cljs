(ns ui.utils
  (:require
   [re-frame.core :as rf]
   [arrival-test-task.fetch :as fetch]))

(def backend-url "http://localhost:3000")

(rf/reg-event-fx
 ::switch-database
 (fn [{db :db} [_ db-name]]
   (when-not (:fetching? db)
     {:fetch-promise
      (-> (fetch/fetch-promise {:uri (str backend-url "/switch-database")
                                :method "POST"
                                :params (cond-> {}
                                          db-name (assoc :db-name db-name))})
          (.then (fn [x] (rf/dispatch [:set-values-by-paths {:db-name (:data x)}]))))})))
