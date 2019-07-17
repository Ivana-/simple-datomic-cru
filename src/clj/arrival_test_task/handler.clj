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

  (POST  "/switch-database" {query-string :query-string} (-> query-string
                                                             parse-query-params
                                                             datomic/switch-database
                                                             add-cors-headers))
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

(comment
  dev-handler

  (defn handler [request]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body "Hello World"})

  (dev-handler 123)

  ; ;; handler argument - incoming request
  ; {:ssl-client-cert nil, :protocol "HTTP/1.1", :remote-addr "0:0:0:0:0:0:0:1", 
  ;  :params {}, :route-params {}, 
  ;  :headers {"origin" "http://localhost:3449", "host" "localhost:3000", 
  ;            "user-agent" "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36", 
  ;            "content-type" "text/clojure", "referer" "http://localhost:3449/", 
  ;            "connection" "keep-alive", "accept" "text/clojure", 
  ;            "accept-language" "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7", "accept-encoding" "gzip, deflate, br"}, 
  ;  :server-port 3000, :content-length nil, 
  ;  :compojure/route [:get "/order"], :content-type "text/clojure", 
  ;  :character-encoding nil, :uri "/order", :server-name "localhost", 
  ;  :query-string "date-from=2019-07-01", 
  ;  :body #object[org.eclipse.jetty.server.HttpInputOverHTTP 0x3459851e "HttpInputOverHTTP@3459851e[c=0,q=0,[0]=null,s=STREAM]"], 
  ;  :scheme :http, :request-method :get}

  ; {:ssl-client-cert nil, :protocol "HTTP/1.1", :remote-addr "0:0:0:0:0:0:0:1", 
  ;  :params {:id "17592186045422"}, :route-params {:id "17592186045422"}, 
  ;  :headers {"origin" "http://localhost:3449", "host" "localhost:3000", 
  ;            "user-agent" "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36", 
  ;            "content-type" "text/clojure", "referer" "http://localhost:3449/", 
  ;            "connection" "keep-alive", "accept" "text/clojure", 
  ;            "accept-language" "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7", "accept-encoding" "gzip, deflate, br"}, 
  ;  :server-port 3000, :content-length nil, 
  ;  :compojure/route [:get "/order/:id"], :content-type "text/clojure", 
  ;  :character-encoding nil, :uri "/order/17592186045422", :server-name "localhost", 
  ;  :query-string nil, 
  ;  :body #object[org.eclipse.jetty.server.HttpInputOverHTTP 0x213bd797 "HttpInputOverHTTP@213bd797[c=0,q=0,[0]=null,s=STREAM]"], 
  ;  :scheme :http, :request-method :get}


  (dev-handler {:uri "/order"
                :query-string "date-from=2019-07-01"
                :request-method :get})
  ;;
  )
