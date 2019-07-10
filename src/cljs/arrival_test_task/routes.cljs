(ns arrival-test-task.routes
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.History)
  (:require
   [secretary.core :as secretary]
   [goog.events :as gevents]
   [goog.history.EventType :as EventType]
   [re-frame.core :as rf]))

;; https://github.com/clj-commons/secretary

(rf/reg-event-db
 ::set-active-panel
 (fn [db [_ active-panel]]

   ;; (prn "active-panel" active-panel)

   (assoc db :active-panel (assoc active-panel :is-fetching true))))

(defn hook-browser-navigation! []
  (doto (History.)
    (gevents/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(secretary/set-config! :prefix "#")

;; define routes here

(defroute "/order" {:as params}
  (rf/dispatch [::set-active-panel (merge params {:page :order-grid})]))

(defroute "/order/new" {:as params}
  (rf/dispatch [::set-active-panel (merge params {:page :order-new})]))

; (defroute "/order/edit" {:as params}
;   (rf/dispatch [::set-active-panel (merge params {:page :order-edit})]))

; (defroute "/auth#:auth" {:as params}
;   (rf/dispatch [:events/auth (:auth params)]))

;; must be at the end, cause routes matches by order
(defroute "*" []
  (rf/dispatch [::set-active-panel {:page :home-panel}]))

;; --------------------
(hook-browser-navigation!)
