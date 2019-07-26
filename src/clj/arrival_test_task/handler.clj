(ns arrival-test-task.handler
  (:require [compojure.core :refer [GET POST OPTIONS defroutes]]
            [compojure.route :refer [resources]]
            [clojure.string :as str]
            [ring.util.response :refer [resource-response]]
            [arrival-test-task.datomic :as datomic]))


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
  
  (POST "/clear-test-database" req (datomic/clear-test-database req))

  (GET  "/order"               req (datomic/order-list req))

  (GET  "/order/:id"           req (datomic/order-by-id req))
  
  (GET  "/order-history/:id"   req (datomic/order-history-by-id req))
  
  (POST "/order-save"          req (datomic/order-save req))

  (resources "/"))

;; (def dev-handler (-> #'routes wrap-reload))

(defn parse-query-params [query-string]
  (->> (str/split (str query-string) #"&")
       (map #(str/split % #"=" 2))
       (reduce (fn [acc [k v]] (assoc acc (keyword k) v)) {})))

; (defn wrap-test-db [handler]
;   (fn [req]
;     (handler (assoc req :test-db? true))))

(defn wrap-request [handler test-db?]
  (fn [{:keys [query-string body] :as req}]
    (handler (cond-> req
               query-string (assoc :query-params (parse-query-params query-string))
               body (update :body (fn [x] (try
                                            ;; clojure.edn/read-string
                                            (-> x slurp read-string)
                                            (catch Exception e x))))
               test-db? (assoc :test-db? true)))))

; (defn get-response-from-error
;   "Converts a thrown error into the response that should be
;    sent back to the client."
;   [error]
;   (let [type (get (ex-data error) :type)]
;     (case type
;       :not-found {:status 404 :body "Not found"}
;       {:status 500 :body "Internal server error"})))
; (defn wrap-error-handling
;   "Ring middleware that catches any thrown errors and sends an appropriate
;    response back to the client."
;   [handler]
;   (fn [req]
;     (try
;       (handler req)
;       (catch Exception e
;         (get-response-from-error e)))))


(defn wrap-response [handler]
  (fn [req]
    (let [res (handler req)]
      (-> res
          (update :body pr-str)
          (assoc-in [:headers "Content-type"] "text/clojure")
          (assoc-in [:headers "Access-Control-Allow-Origin"] "*")))))

(def dev-handler
  (-> routes
      (wrap-request false)
      wrap-response))

(def test-handler
  (-> routes
      (wrap-request true)
      wrap-response))


(comment
  dev-handler

  (defn handler [request]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body "Hello World"})

  (dev-handler 123)
  
  ; {:ssl-client-cert nil, 
;  :protocol "HTTP/1.1", 
;  :remote-addr "0:0:0:0:0:0:0:1", 
;  :params {:id "17592186045422"}, 
;  :route-params {:id "17592186045422"}, 
;  :headers {"origin" "http://localhost:3449", 
;            "host" "localhost:3000", 
;            "user-agent" "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36", 
;            "content-type" "text/clojure", 
;            "referer" "http://localhost:3449/", 
;            "connection" "keep-alive", 
;            "pragma" "no-cache", 
;            "accept" "text/clojure", 
;            "accept-language" "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7", 
;            "accept-encoding" "gzip, deflate, br", 
;            "cache-control" "no-cache"}, 
;  :server-port 3000, 
;  :content-length nil, 
;  :compojure/route [:get "/order/:id"], 
;  :content-type "text/clojure", 
;  :character-encoding nil, 
;  :uri "/order/17592186045422", 
;  :server-name "localhost", 
;  :query-string nil, 
;  :body #object[org.eclipse.jetty.server.HttpInputOverHTTP 0x7045e554 "HttpInputOverHTTP@7045e554[c=0,q=0,[0]=null,s=STREAM]"], 
;  :scheme :http, 
;  :request-method :get
;  :test-db? true}

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
