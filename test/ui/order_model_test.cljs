(ns ui.order-model-test
  (:require
   [cljs.test :refer-macros [deftest is testing run-tests      use-fixtures async]]
   [day8.re-frame.test :as rf-test]
  ;  [matcho.core :as matcho]
   [re-frame.core :as rf]
   [arrival-test-task.order-grid :as sut]
   [ui.macroses :refer-macros [wait-for-fetch-finished]]
   [ui.utils :as utils]
   [arrival-test-task.order-grid :as sut]
   [clojure.string :as str]
   ;;
   ))


(comment
  (macroexpand-1 '(wait-for-fetch-finished [a b] [c d e] [f g]))
  ; (reset! re-frame.db/app-db {})
  ; (prn :app-db @re-frame.db/app-db)
  ;;
  )


(use-fixtures :once
  {:before #(async done
                   (prn "Before..." %)
                   (done))
   :after #(do (prn "After..." %))})


(defn parse-date [s]
  (if-not (str/blank? s)
    (let [date (js/Date. (js/Date.parse s))
          offset-ms (* 60 1000 (.getTimezoneOffset date))
          ms-utc (+ (.getTime date) offset-ms)
          date-utc (js/Date. ms-utc)]
      date-utc)))

(deftest grid
  (rf-test/run-test-async ;; run-test-sync
   (let [order-template {;; :db/id 17592186045422
                         :order/title "Some title"
                         :order/description "Lorem ipsum"
                         :order/applicant "Ivanov"
                         :order/performer "Petrov"
                         :order/date "2019-07-14"}
         order-template-1 (assoc order-template
                                 :order/title "Second title"
                                 :order/date "2019-07-16")
         m (rf/subscribe [:get-values-by-paths {:db-name :db-name
                                                :order :order
                                                :order-list :order-list
                                                :order-history :order-history}])]

     (rf/dispatch-sync [:set-values-by-paths {:order order-template
                                              :order-history nil
                                              :order-list nil}])
     (is (= (-> @m :order) order-template))

     (rf/dispatch [::utils/switch-database "test"]) ;; switch to test database

     (wait-for-fetch-finished

      [(is (= (-> @m :db-name) {:db-name "test"}))
       (rf/dispatch [::sut/save-order])]

      [(let [[order-1-id order-1] (-> @m :order-list first)]

         ;; (prn (-> @m :order-list))
         (is (= (dissoc order-1 :db/id)
                (update order-template :order/date parse-date)))
         (is (= (select-keys @m [:order :order-list :order-history])
                {:order nil
                 :order-history nil
                 :order-list {order-1-id order-1}}))

         (rf/dispatch [::sut/fetch-order order-1-id])

         (wait-for-fetch-finished
          [(is (= (select-keys @m [:order :order-list :order-history])
                  {:order (assoc order-template :db/id order-1-id)
                   :order-history nil
                   :order-list {order-1-id order-1}}))
           (rf/dispatch [::sut/fetch-order-history order-1-id])]

          [(is (= (->> (:order-history @m)
                       (reduce (fn [acc [k v]] (assoc acc k (mapv second v))) {}))
                  {order-1-id [(dissoc order-1 :db/id)]}))
           
           (rf/dispatch-sync [:set-values-by-paths
                              {:order (assoc order-1
                                             :order/date "2019-07-15"
                                             :order/title "New title")}])
           (rf/dispatch [::sut/save-order])]

          [(is (= (select-keys @m [:order :order-list])
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
           (rf/dispatch [::sut/save-order])]

          [(is (= (-> @m :order-list count) 2))
           (is (= (-> @m :order) nil))

           ;; here we have order-1-id in history
           ;; dispatching again deletes it from db without fetching
           (rf/dispatch-sync [::sut/fetch-order-history order-1-id])
           (is (= (-> @m :order-history) {}))
           
           (rf/dispatch-sync [:set-values-by-paths {:date-from nil :date-to "2019-07-15"}])
           (rf/dispatch [::sut/load-order-list])]

          [(is (= (select-keys @m [:order :order-list])
                  {:order nil
                   :order-list {order-1-id (assoc order-1
                                                  :order/date (parse-date "2019-07-15")
                                                  :order/title "New title")}}))
           (rf/dispatch-sync [:set-values-by-paths {:date-from "2019-07-16" :date-to nil}])
           (rf/dispatch [::sut/load-order-list])]

          [(is (= (-> @m :order-list count) 1))
           (is (= (-> @m :order-list vals first (dissoc :db/id))
                  (update order-template-1 :order/date parse-date)))
           (rf/dispatch [::utils/switch-database])]

          [(is (= (-> @m :db-name) {:db-name "hello"}))]
            ;;
          ))]))
       ;;
   ))


(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (if (cljs.test/successful? m)
    (println "Success!")
    (println "FAIL")))


(comment
  (run-tests)

  (identity @re-frame.db/app-db)

  (js/Date. (js/Date.parse "2019-07-14"))
  (parse-date "2019-07-14")
  ;;
  )