(ns ui.order-model-test
  (:require ;; [cljs.test :refer-macros [deftest is testing run-tests      use-fixtures async]]
   [clojure.test :refer [deftest is testing run-tests      use-fixtures async]]
   [day8.re-frame.test :as t]
            ;; [matcho.core :as matcho]
   [re-frame.core :as rf]
   [ui.macroses :refer-macros [<<-]]
   [ui.utils :as utils]
   [arrival-test-task.config :as config]
   [clojure.string :as str]
   [arrival-test-task.order-grid :as sut]
   [arrival-test-task.events]))

(deftest ttt
  (testing "testing test"
    (is (= 3 (<<- (+ 1 2))))))



(comment
  (macroexpand '(<<-(a b 1) (c d 2) (e f 3)))
  ; (reset! re-frame.db/app-db {})
  ; (prn :app-db @re-frame.db/app-db)
  ;;
  )


; (use-fixtures :once
;   {:before #(async done
;                    (prn "Before..." %)
;                    (done))
;    :after #(do
;              (prn "After..." %)
;              ;; (rf/dispatch-sync [:set-values-by-paths {:config {:backend-url "zazaza"}}])
;              )})


(defn parse-date [s]
  (if-not (str/blank? s)
    (let [date (js/Date. (js/Date.parse s))
          offset-ms (* 60 1000 (.getTimezoneOffset date))
          ms-utc (+ (.getTime date) offset-ms)
          date-utc (js/Date. ms-utc)]
      date-utc)))


(deftest grid
  (t/run-test-async ;; run-test-sync
   (let [order-template {;; :db/id 17592186045422
                         :order/title "Some title"
                         :order/description "Lorem ipsum"
                         :order/applicant "Ivanov"
                         :order/performer "Petrov"
                         :order/date "2019-07-14"}
         order-template-1 (assoc order-template
                                 :order/title "Second title"
                                 :order/date "2019-07-16")
         m (rf/subscribe [:get-values-by-paths {:config :config
                                                :db-name :db-name
                                                :order :order
                                                :order-list :order-list
                                                :order-history :order-history
                                                :fetching? :fetching?
                                                :error :error}])]
     ;; switch to test url
     (rf/dispatch-sync [:set-values-by-paths {:config {:backend-url config/test-backend-url}}])
     (is (= (-> @m :config :backend-url) config/test-backend-url))

     (rf/dispatch [::utils/clear-test-database])

     (<<-

      (t/wait-for [:fetch-finished]
                  (is (= (-> @m :db-name) {:db-name "test"}))

                  (when-not (= (-> @m :db-name) {:db-name "test"})
                      (throw (js/Error. "Did not switch to test database!")))

                  (rf/dispatch-sync [:set-values-by-paths {:order order-template
                                                           :order-history nil
                                                           :order-list nil}])
                  (is (= (-> @m :order) order-template))

                  (rf/dispatch [::sut/save-order]))

      (t/wait-for [:fetch-finished]
                  ;; (let [[order-1-id order-1] (-> @m :order-list first)]
                  (def order-1-id (-> @m :order-list keys first))
                  (def order-1 (-> @m :order-list vals first))

                  ;; (prn (-> @m :order-list))
                  (is (= (dissoc order-1 :db/id)
                         (update order-template :order/date parse-date)))
                  (is (= (select-keys @m [:order :order-list :order-history])
                         {:order nil
                          :order-history nil
                          :order-list {order-1-id order-1}}))

                  (rf/dispatch [::sut/fetch-order order-1-id]))

      (t/wait-for [:fetch-finished]
                  (is (= (select-keys @m [:order :order-list :order-history])
                         {:order (assoc order-template :db/id order-1-id)
                          :order-history nil
                          :order-list {order-1-id order-1}}))
                  (rf/dispatch [::sut/fetch-order-history order-1-id]))

      (t/wait-for [:fetch-finished]
                  (is (= (->> (:order-history @m)
                              (reduce (fn [acc [k v]] (assoc acc k (mapv second v))) {}))
                         {order-1-id [(dissoc order-1 :db/id)]}))

                  (rf/dispatch-sync [:set-values-by-paths
                                     {:order (assoc order-1
                                                    :order/date "2019-07-15"
                                                    :order/title "New title")}])
                  (rf/dispatch [::sut/save-order]))

      (t/wait-for [:fetch-finished]
                  (is (= (select-keys @m [:order :order-list])
                         {:order nil
                          :order-list {order-1-id (assoc order-1
                                                         :order/date (parse-date "2019-07-15")
                                                         :order/title "New title")}}))
                  (is (= (->> (:order-history @m)
                              (reduce (fn [acc [k v]] (assoc acc k (mapv second v))) {}))
                         {order-1-id [(dissoc order-1 :db/id)
                                      {:order/date (parse-date "2019-07-15")
                                       :order/title "New title"}]}))
                  (rf/dispatch-sync [:set-values-by-paths {:order order-template-1}])
                  (rf/dispatch [::sut/save-order]))

      (t/wait-for [:fetch-finished]
                  (is (= (-> @m :order-list count) 2))
                  (is (= (-> @m :order) nil))

                  ;; here we have order-1-id in history
                  ;; dispatching again deletes it from db without fetching
                  (rf/dispatch-sync [::sut/fetch-order-history order-1-id])
                  (is (= (-> @m :order-history) {}))

                  (rf/dispatch-sync [:set-values-by-paths {:date-from nil :date-to "2019-07-15"}])
                  (rf/dispatch [::sut/load-order-list]))

      (t/wait-for [:fetch-finished]
                  (is (= (select-keys @m [:order :order-list])
                         {:order nil
                          :order-list {order-1-id (assoc order-1
                                                         :order/date (parse-date "2019-07-15")
                                                         :order/title "New title")}}))
                  (rf/dispatch-sync [:set-values-by-paths {:date-from "2019-07-16" :date-to nil}])
                  (rf/dispatch [::sut/load-order-list]))

      (t/wait-for [:fetch-finished]
                  (is (= (-> @m :order-list count) 1))
                  (is (= (-> @m :order-list vals first (dissoc :db/id))
                         (update order-template-1 :order/date parse-date)))
                  (rf/dispatch-sync [:set-values-by-paths {:date-from "zazaza" :date-to nil}])
                  (rf/dispatch [::sut/load-order-list]))

      (t/wait-for [:fetch-finished]
                  ;; (prn (:error @m))                  
                  (is (-> @m :error boolean))
                  (is (= (:error @m) "Unparseable date: \"zazaza\""))

                  ; (rf/dispatch-sync [:set-values-by-paths {:config {:backend-url config/backend-url}}])
                  ; (is (= (-> @m :config :backend-url) config/backend-url))
                  )

      ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
      ))))


; (defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
;   (if (cljs.test/successful? m)
;     (println "Success!")
;     (println "FAIL")))


(comment
  (enable-console-print!)
  (run-tests)

  (identity @re-frame.db/app-db)

  (js/Date. (js/Date.parse "2019-07-14"))
  (parse-date "2019-07-14")
  ;;
  )