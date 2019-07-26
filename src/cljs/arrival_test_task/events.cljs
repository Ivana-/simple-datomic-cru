(ns arrival-test-task.events
  (:require
   [re-frame.core :as rf]
   [arrival-test-task.config :as config]))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _] {:config {:backend-url config/backend-url}}))

(rf/reg-event-db
 :set-values-by-paths
 (fn [db [_ paths-values]] (reduce (fn [a [k v]] ((if (vector? k) assoc-in assoc) a k v)) db paths-values)))

(rf/reg-sub
 :get-values-by-paths
 (fn [db [_ keys-paths]] (reduce (fn [a [k p]] (assoc a k ((if (vector? p) get-in get) db p))) {} keys-paths)))
