(ns arrival-test-task.handler
  (:require [compojure.core :refer [GET POST OPTIONS defroutes]]
            [compojure.route :refer [resources]]
            [clojure.string :as str]
            [ring.util.response :refer [resource-response]]
            ;; [ring.middleware.reload :refer [wrap-reload]]
            ;; [ring.middleware.format :refer [wrap-restful-format]]
            [arrival-test-task.datomic :as datomic]))

(defn parse-query-params [query-string]
  (->> (str/split (str query-string) #"&")
       (map #(str/split % #"=" 2))
       (reduce (fn [acc [k v]] (assoc acc (keyword k) v)) {})))

(defn add-cors-headers [x] (-> x
                               (assoc :headers {"Access-Control-Allow-Origin" "*"})
                               (update :body pr-str)))

(def test-responce
  {:status 200
   :body (-> (let [x {1 1
                      "2" "2"
                      :a :a
                      [1 "2" :a] [1 "2" :a]
                      "xyz" #{1 "2" :a}}]
               (merge x {x x})))})

(defroutes routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (OPTIONS
    #_"/cors"
    "*" [] {:status 200
            :headers {"Access-Control-Allow-Origin" "*"
                      "Access-Control-Allow-Methods" "GET, POST, PATCH, PUT, DELETE"
                      "Access-Control-Allow-Headers" "Accept, Content-Type" ; "Origin, Content-Type, X-Auth-Token"
                      }})
  ;; (GET "/user/:id" [id] (str "<h1>Hello user " id "</h1>"))
  (GET  "/order" {query-string :query-string} (-> query-string
                                                  parse-query-params
                                                  datomic/order-list
                                                  add-cors-headers))
  (GET  "/order/:id" [id] (-> id
                              read-string
                              datomic/order-by-id
                              add-cors-headers))
  (GET  "/order-history/:id" [id] (-> id
                                      read-string
                                      datomic/order-history-by-id
                                      add-cors-headers))
  ; (POST "/order-save" {body-params :body-params} (-> body-params
  ;                                                    datomic/order-save
  ;                                                    add-cors-headers))
  (POST "/order-save" {body :body} (-> body
                                       slurp
                                       ;; clojure.edn/read-string
                                       read-string
                                       datomic/order-save
                                       add-cors-headers))

  (GET  "/test" [] (-> test-responce add-cors-headers))
  
  (resources "/"))

;; (def dev-handler (-> #'routes wrap-reload))

(def dev-handler
  (-> routes
      ; (wrap-restful-format
      ; ;  :formats [:json] ;; [:json-kw]
      ; ;  :response-options {:json {:pretty true}} ;; {:json-kw {:pretty true}}
      ;    )
      ))

(def handler routes)
