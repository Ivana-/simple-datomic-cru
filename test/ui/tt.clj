(ns ui.tt
  (:require
;    [matcho.core :as matcho]
   [re-frame.core :as rf]
;    [testing.fx :as fx]
;    [testing.gen :as gen]
;    [app.db :as db]
   [clojure.test :refer :all]
;   [ui.encounters.grid-model :as m]
   ;; [arrival-test-task.events]
   ;; [arrival-test-task.handler :as handler]
   ))

(deftest grid

  (is (= (count [1 2 3]) 3))

  ; (rf/dispatch [::model/test-fx "zazaza"])

  ; (def m (rf/subscribe [::model/get-value [:test]]))

  ; (def m (rf/subscribe [:get-values-by-paths {:test :test}]))


  ; (identity @m)

  ; (is (= 2 (count (:items @m))))
  
  ; model/backend-url


  ; (reset! re-frame.db/app-db {;;
  ;                             :date-from "sdrgsdfgsdfgsd"
  ;                             ;; :date-from "2019-07-14" ;; "2019-07-15"
  ;                             :date-to ""
  ;                             :text-search ""})

  ; (rf/dispatch [::model/load-order-list])
  
  ; (rf/dispatch [::model/db-test 345])
  
  ; ::model/load-order-list
  
  ; ; ::load-order-list
  ; ; ::save-order
  ; ; ::fetch-order
  ; ; ::fetch-order-history
  
  ; (def m (rf/subscribe [:get-values-by-paths {:order-list :order-list}]))

  ; (identity @m)
  ; (identity @re-frame.db/app-db)


  
  ; (reset! re-frame.db/app-db {})
  ; (rf/dispatch [::model/fetch-test 17592186045422])
  ; (rf/dispatch [::model/fetch-test 333])

  ; (identity @re-frame.db/app-db)

  ;;
  )

(comment
  (run-tests)
  ;;
  )

; (run-tests)
; (deftest grid

;   (gen/bronx)

;   (def encounters (gen/a! [:patient :appointment {:encounter 3}]
;                           {:encounter [{:period {:start "2019-02-23"}}
;                                        {:period {:start "2019-02-20"}}
;                                        {:period {:start "2019-02-23"}
;                                         :status "cancelled"}]}))

;   (is (= (count (map #(get-in % [:period])
;                      (fx/http [:get* "Encounter"])))
;          3))

;   (rf/dispatch [:encounters/grid :init {:params {:date "2019-02-22"}}])

;   (def m (rf/subscribe [:encounters/grid]))

;   (is (= 2 (count (:items @m))))

;   (matcho/assert
;    ^:matcho/strict
;    ["2019-02-23"
;     "2019-02-23"]
;    (map :start-time (:items @m)))


;   (rf/dispatch [:encounters/grid :init {:params {:date "2019-02-22"
;                                                  :status "cancelled"}}])

;   (def m (rf/subscribe [:encounters/grid]))

;   (is (= 1 (count (:items @m))))

;   (matcho/assert
;    ^:matcho/strict
;    ["cancelled"]
;    (map :status (:items @m)))

;   (rf/dispatch [:encounters/grid :init {:params {:date "2019-02-19"}}])

;   (def m (rf/subscribe [:encounters/grid]))

;   (is (= 1 (count (:items @m))))


;   (rf/dispatch [:encounters/grid :init {:params {:date "2019-02-10"}}])

;   (def m (rf/subscribe [:encounters/grid]))

;   (is (= 0 (count (:items @m)))))
