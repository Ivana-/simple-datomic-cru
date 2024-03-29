(ns arrival-test-task.fetch
  (:require [re-frame.core :as rf]
            [clojure.string :as str]
            [cljs.reader :as reader]))

(defn to-query [params]
  (->> params
       (mapv (fn [[k v]] (str (name k) "=" v)))
       (str/join "&")))

; ; (defn kw-2-str [x] (let [kw-ns (namespace x)
; ;                          kw-nm (name x)]
; ;                      (if (str/blank? kw-ns) kw-nm (str kw-ns "/" kw-nm))))

; (defn kw-2-str [x] (subs (str x) 1))

; (defn clj->js-ns [x] (clj->js x :keyword-fn kw-2-str))

(defn fetch-promise [{:keys [uri token headers params] :as opts}]
  (let [content-type "text/clojure" ;; "application/json"
        headers (merge (or headers {})
                       {"Accept" content-type
                        "Content-Type" content-type})
        fetch-opts (-> (merge {:method "GET"
                               :mode "cors"} opts)
                       (dissoc :uri :headers :params)
                       (assoc :headers headers))
        fetch-opts (if (:body opts)
                     ;; (assoc fetch-opts :body (.stringify js/JSON (clj->js-ns (:body opts))))
                     (assoc fetch-opts :body (pr-str (:body opts))) ;; for text body
                     fetch-opts)
        url uri]
    (->
     (js/fetch (str url (when params (str "?" (to-query params)))) (clj->js fetch-opts))

     ;; (.then (fn [resp] (js/Promise.all [resp (.json resp)]))) ;; for json body
     (.then (fn [resp] (js/Promise.all [resp (.text resp)]))) ;; for text body

     (.then (fn [[resp doc]] (let [;; data (js->clj doc :keywordize-keys true) ;; for json body
                                   data (reader/read-string doc) ;; for text body
                                   res {:request opts
                                        :data data}]
                               (if (> (.-status resp) 299)
                                 (let [e (js/Error. (str "failed to fetch " uri))]
                                   ;;(aset e "params" res)
                                   (set! (.-params e) res)
                                   (throw e))
                                 (js/Promise.resolve res)))))

      ;; (.catch (fn [e] (throw (js/Error. (str "failed to fetch " uri)))))
     )))

; (defn error-data [e] (.-params e))
; (defn error-message [e] (.-message e))


(rf/reg-event-db
 :fetch-finished
 (fn [db [_ x]]
   ;; (prn :fetch-finished)
   (merge db x)))

(rf/reg-fx
 :fetch-promise
 (fn [p]
   (rf/dispatch [:set-values-by-paths {:fetching? true}])
   (-> p
       (.then (fn [_] (rf/dispatch [:fetch-finished {:error nil
                                                     :fetching? false}])))
       (.catch (fn [e] (let [{{message :message} :data} (.-params e)]
                         (rf/dispatch [:fetch-finished {:error (or message (.-message e))
                                                        :fetching? false}])))))))
