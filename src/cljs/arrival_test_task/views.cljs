(ns arrival-test-task.views
  (:require
   ;; [re-frame.core :as rf]
   [arrival-test-task.order-grid :as order-grid]
   ;; [arrival-test-task.order-form :as order-form]
   ))

;; preparings for future client routing - now disabled

(def routes
  (merge
   order-grid/routes
   ;; order-form/routes
   ))

; (rf/reg-sub
;  ::active-panel
;  (fn [db _] (:active-panel db)))

(defn main-panel []
  ((:order-grid routes))
  ; (let [params @(rf/subscribe [::active-panel])]
  ;   (if-let [route-fn (get routes (:page params))]
  ;     [route-fn params]
  ;     ;; [:div {:style {:font-size "30px"}} (str "No matched route " params)]
  ;     [:div
  ;      [:h1 (str "Hello from fhir-face. This is the Home Page.")]
  ;      [:div [:a {:href "#/order"} "go to Order Grid Page"]]
  ;      [:br]
  ;      [:div [:a {:href "#/order/new"} "go to New Order Page"]]
  ;      [:br]]))
  )
