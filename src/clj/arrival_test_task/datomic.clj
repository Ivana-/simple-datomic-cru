(ns arrival-test-task.datomic
  (:require [datomic.api :as d]))

;; peer library

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; start database connections, schema loading, etc.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn try-chain [m f err-str]
  (if (:error m)
    m
    (try
      (merge m (f m))
      (catch Exception e
        (assoc m :error (str (.getMessage e) " " err-str
                             " Check if datomic is installed and transactor is running"))))))

(def context
  (-> {:db-domain-host-port "datomic:dev://localhost:4334/"
       :db-name "hello"
       ;; title, description, applicant, performer and date
       :schema [{:db/ident :order/title
                 :db/valueType :db.type/string
                 :db/cardinality :db.cardinality/one
                 :db/doc "The title of the order"}

                {:db/ident :order/description
                 :db/valueType :db.type/string
                 :db/cardinality :db.cardinality/one
                 :db/doc "The description of the order"}
                
                {:db/ident :order/applicant
                 :db/valueType :db.type/string
                 :db/cardinality :db.cardinality/one
                 :db/doc "The applicant of the order"}
                
                {:db/ident :order/performer
                 :db/valueType :db.type/string
                 :db/cardinality :db.cardinality/one
                 :db/doc "The performer of the order"}

                {:db/ident :order/date
                 :db/valueType :db.type/instant
                 :db/cardinality :db.cardinality/one
                 :db/doc "The date of the order"}]}

      (#(assoc % :db-uri (str (:db-domain-host-port %) (:db-name %))))

      (try-chain (fn [m] {:database-names
                          (d/get-database-names (str (:db-domain-host-port m) "*"))})
                 "get-database-names error")

      (try-chain (fn [m]
                   (when-not (some #(= (:db-name m) %) (:database-names m))
                     (d/create-database (:db-uri m)))
                   {:db-created? true})
                 "create-database error")

      (try-chain (fn [m] {:conn (d/connect (:db-uri m))})
                 "connect-database error")

      (try-chain (fn [m]
                   ;; @(d/transact (:conn m) (:schema m))
                   (let [db (d/db (:conn m))]
                     (doseq [{db-ident :db/ident :as attr} (:schema m)]
                       (when (empty? (d/q [:find '?e
                                           :where
                                           ['?e :db/ident db-ident]] db))
                         (prn "add attribute" attr)
                         @(d/transact (:conn m) [attr])))
                     {:schema-transacted? true}))
                 "transact-schema error")
      ;;
      ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; queries/transactions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro try-wraps [body]
  `(if-let [context-error# (:error context)]
    {:status 400
     :body {:message context-error#}}
    (try
      {:status 200
       :body ~body}
      (catch Exception e#
        {:status 400
         :body {:message (.getMessage e#)}}))))

;; (macroexpand-1 '(try-wraps (1 2 3)))

(def simple-date-format (java.text.SimpleDateFormat. "yyyy-MM-dd"))

(defn parse-date [s] (.parse simple-date-format (str s)))

(defn order-list [{:keys [date-from date-to] :as params}]
  (try-wraps
   (let [db (d/db (:conn context))]
     (->>
      (d/q (filterv identity
                    [:find '?e '?title '?description '?applicant '?performer '?date
                     :where
                     ['?e :order/title]
                     ['?e :order/title '?title]
                     ['?e :order/description '?description]
                     ['?e :order/applicant '?applicant]
                     ['?e :order/performer '?performer]
                     ['?e :order/date '?date]
                     (when date-from [(list '>= '?date (parse-date date-from))])
                     (when date-to   [(list '<= '?date (parse-date date-to))])
                     ;;
                     ])
           db)
      (reduce (fn [acc v] (assoc acc (first v) (zipmap [:db/id
                                                        :order/title
                                                        :order/description
                                                        :order/applicant
                                                        :order/performer
                                                        :order/date] v))) {})))))

(defn order-by-id [id]
  (try-wraps
   (d/pull (d/db (:conn context)) '[*] id)))

(defn order-history-by-id [id]
  (try-wraps
   (let [db (d/db (:conn context))
         hdb (d/history db)]
     (->> id
          (d/q
           '[:find ?e ?attr ?v ?tx ?added ?inst
             :in $ ?e
             :where
             [?e ?a ?v ?tx ?added]
             [?tx :db/txInstant ?inst]
             [?a :db/ident ?attr]]
           hdb)
          (reduce (fn [acc [e a v tx added inst]] (if added
                                                    (update acc inst #(assoc % a v))
                                                    acc)) {})
          (sort-by first)))))

(defn order-save [params]
  (try-wraps
   (let [t @(d/transact (:conn context) [(update params :order/date parse-date)])
         id (or (:db/id params) ;; update existing id
                (-> (:tempids t) vals first) ;; created id on create resource
                )]
     ;; (order-by-id id)
     (d/pull (d/db (:conn context)) '[*] id))))


; (prn "=============== transaction " t)

; ; create with new id!!!
; {:db-before ;; datomic.db.Db@d5a7e1c1, 
;  :db-after ;; datomic.db.Db@c25940d5, 
;  :tx-data [#datom[13194139534348 50 #inst "2019-07-07T23:29:44.495-00:00" 13194139534348 true]
;            #datom[17592186045453 75 "t3" 13194139534348 true]
;            #datom[17592186045453 77 "a3" 13194139534348 true]
;            #datom[17592186045453 78 "p3" 13194139534348 true]
;            #datom[17592186045453 79 #inst "2019-07-07T21:00:00.000-00:00" 13194139534348 true]
;            #datom[17592186045453 76 "description" 13194139534348 true]]
;  :tempids {-9223301668109598140 17592186045453}}
; ; !!!! created id: 17592186045453

; ; update by id!!!!
; {:db-before ;; datomic.db.Db@8ad89554, 
;  :db-after ;; datomic.db.Db@72db3ff8, 
;  :tx-data [#datom[13194139534357 50 #inst "2019-07-07T23:54:45.820-00:00" 13194139534357 true]
;            #datom[17592186045459 75 "t7__" 13194139534357 true]
;            #datom[17592186045459 75 "t7_" 13194139534357 false]]
;  :tempids {}}
; ; !!!! updated existing id 17592186045459



;; @(d/transact conn first-movies)
; {:db-before datomic.db.Db@c4ea5c4e, 
; :db-after datomic.db.Db@59a115a0, 
; :tx-data [#datom[13194139534319 50 #inst "2019-07-03T17:50:13.106-00:00" 13194139534319 true] 
; #datom[17592186045424 72 "The Goonies" 13194139534319 true] 
; #datom[17592186045424 73 "action/adventure" 13194139534319 true] 
; #datom[17592186045424 74 1985 13194139534319 true] 
; #datom[17592186045425 72 "Commando" 13194139534319 true] 
; #datom[17592186045425 73 "action/adventure" 13194139534319 true] 
; #datom[17592186045425 74 1985 13194139534319 true] 
; #datom[17592186045426 72 "Repo Man" 13194139534319 true] 
; #datom[17592186045426 73 "punk dystopia" 13194139534319 true] 
; #datom[17592186045426 74 1984 13194139534319 true]], 
; :tempids {-9223301668109598139 17592186045424,
; -9223301668109598138 17592186045425,
; -9223301668109598137 17592186045426}}

; {:db-before {:database-id "58a47389-f1ab-4d81-85b6-715cecde9bac", 
;              :t 1000, 
;              :next-t 1001, 
;              :history false}, 
;  :db-after {:database-id "58a47389-f1ab-4d81-85b6-715cecde9bac", 
;             :t 1001, 
;             :next-t 1005, 
;             :history false}, 
;  :tx-data [ #datom[13194139534317 50 #inst "2017-02-15T19:28:52.270-00:00" 13194139534317 true] 
;             #datom[17592186045422 63 "The Goonies" 13194139534317 true] 
;             #datom[17592186045422 64 "action/adventure" 13194139534317 true] 
;             #datom[17592186045422 65 1985 13194139534317 true] 
;             #datom[17592186045423 63 "Commando" 13194139534317 true] 
;             #datom[17592186045423 64 "action/adventure" 13194139534317 true] 
;             #datom[17592186045423 65 1985 13194139534317 true] 
;             #datom[17592186045424 63 "Repo Man" 13194139534317 true] 
;             #datom[17592186045424 64 "punk dystopia" 13194139534317 true] 
;             #datom[17592186045424 65 1984 13194139534317 true]], 
;  :tempids {-9223301668109598138 17592186045422, -9223301668109598137 17592186045423, -9223301668109598136 17592186045424}}


(comment

  (def conn (:conn context))

  ; @(d/transact conn [{:db/doc "Hello world"}])

  (def db (d/db conn))

  (d/get-database-names (str (:db-domain-host-port context) "*"))


  (def x1 (java.text.SimpleDateFormat. "yyyy-MM-dd"))
  (.parse x1 "2014-08-06")
  ;; #inst "2014-08-05T21:00:00.000-00:00"
  (type (.parse x1 "2014-08-06"))


  (parse-date nil)


  (d/q '[:find ?eid ?title ;; ?description ?applicant ?performer ?date
         :in $ ?eid
         :where
         [?eid]]
       db 17592186045440)

  (seq (d/pull db '[*] 17592186045440)) ;; ([:db/id 17592186045440] [:order/title "t2"] [:order/description "d2"] [:order/applicant "a2"] [:order/performer "p2"] [:order/date #inst "2019-07-05T21:00:00.000-00:00"])
  (seq (d/pull db '[*] 17592186045449)) ;; ([:db/id 17592186045449])

  (d/entity db 17592186045440) ;; #:db{:id 17592186045440}
  (d/entity db 17592186045449) ;; #:db{:id 17592186045449}

  (def hdb (d/history db))
  (seq (d/pull hdb '[*] 17592186045440))
  (->> 17592186045440
       (d/q
        '[:find ?e ?attr ?v ?tx ?added ?inst
          :in $ ?e
          :where
          [?e ?a ?v ?tx ?added]
          [?tx :db/txInstant ?inst]
          [?a :db/ident ?attr]]
        (d/history db))
       (reduce (fn [acc [e a v tx added inst]]
                 (if added
                   (update acc inst #(assoc % a v))
                   acc)) {})
       (sort-by first))

  (sort-by first {:b 2 :c 3 :a 1})
  (into [1 2 3] [4 5 6])
  (into [1 2 3] nil)

  (some even? '(1 2 3 4))

  (some #(= % :b) (keys {:a 1 :b 2}))

  ; #{[17592186045440 :order/applicant   "a2" 13194139534335 true  #inst "2019-07-05T16:43:16.370-00:00"] 
  ;   [17592186045440 :order/performer   "p2" 13194139534335 true  #inst "2019-07-05T16:43:16.370-00:00"] 
  ;   [17592186045440 :order/description "d2 add some text to description" 13194139534339 true #inst "2019-07-06T22:42:31.873-00:00"] 
  ;   [17592186045440 :order/title       "t2" 13194139534335 true  #inst "2019-07-05T16:43:16.370-00:00"] 
  ;   [17592186045440 :order/description "d2" 13194139534339 false #inst "2019-07-06T22:42:31.873-00:00"] 
  ;   [17592186045440 :order/date        #inst "2019-07-05T21:00:00.000-00:00" 13194139534335 true #inst "2019-07-05T16:43:16.370-00:00"] 
  ;   [17592186045440 :order/description "d2" 13194139534335 true  #inst "2019-07-05T16:43:16.370-00:00"]}


  (defn entity-history
    "Takes an entity and shows all the transactions that touched this entity.
  Pairs well with clojure.pprint/print-table"
    [db eid]
    (->> eid
         (d/q
          '[:find ?e ?attr ?v ?tx ?added ?inst
            :in $ ?e
            :where
            [?e ?a ?v ?tx ?added]
            [?tx :db/txInstant ?inst]
            [?a :db/ident ?attr]]
          (d/history db))
         (map #(->> %
                    (map vector [:e :a :v :tx :added :inst])
                    (into {})))
         (sort-by :tx)))


  (entity-history db 17592186045440)



  [{:db/ident :movie/title
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The title of the movie"}

   {:db/ident :movie/genre
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The genre of the movie"}

   {:db/ident :movie/release-year
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "The year the movie was released in theaters"}]

  (let [r (d/q '[:find ?e
                 :where
                 [?e :db/ident :movie/title]]db)]
    (empty? r))

  (def all-movies-q '[:find ?e
                      :where [?e :movie/title]])

  (d/q all-movies-q db)


  (try
    (d/q '[:find ?e
           :where [?e :movie/title--]]
         db)
    (catch Exception e (.getMessage e)))


  (d/q '[:find ?e ?title ?year ?genre
         :where
         [?e :movie/title]
         [?e :movie/title ?title]
         [?e :movie/release-year ?year]
         [?e :movie/genre ?genre]
         ;; [?e :movie/release-year 1985]
         [(>= ?year 1991)]]
       db)

  ; {:query
  ;  {:find [(avg ?mark) .]
  ;   :with [?v]
  ;   :in [$ ?location ?fromDate ?toDate ?fromAge ?toAge ?gender]
  ;   :where
  ;   [[?v :location ?location]
  ;    [?v :mark ?mark]
  ;    [?v :visited_at ?visited-at]
  ;    [(> ?visited-at ?fromDate)]
  ;    [(< ?visited-at ?toDate)]
  ;    [?v :user ?user]
  ;    [?user :birth_date ?birth-date]
  ;    [(< ?birth-date ?fromAge)]
  ;    [(> ?birth-date ?toAge)]
  ;    [?user :gender ?gender]]}
  ;  :args [<db-object> [:location/id 42] 1504210734 1504280734 20 30 "m"]}

  (d/q (filterv
        identity
        [:find '?e
        ;  '?title '?description '?applicant '?performer 
         '?date
         ;:keys 'id 'date
         :where
        ;  ['?e :order/title]
        ;  ['?e :order/title '?title]
        ;  ['?e :order/description '?description]
        ;  ['?e :order/applicant '?applicant]
        ;  ['?e :order/performer '?performer]
         ['?e :order/date '?date]

        ;  '[?id :id/name ?e]
        ;  '[?date :date/name ?date]

        ;; ['?e :order/date y]
         [(list '<= '?date (parse-date "2019-07-08"))]
         ;;
         ])
       db)

  (list 1 2 3)
  (quot 10 3)




  (def all-titles-q '[:find ?movie-title
                      :where [_ :movie/title ?movie-title]])

  (d/q all-titles-q db)

  (def titles-from-1985 '[:find ?title
                          :where [?e :movie/title ?title]
                          [?e :movie/release-year 1985]])

  (d/q titles-from-1985 db)

  (def tq
    (let [y 1985]
      [:find '?title '?year '?genre
       :where
       ['?e :movie/title '?title]
       ['?e :movie/release-year '?year]
       ['?e :movie/genre '?genre]
        ;; ['?e :movie/release-year y]
       ]))

  (d/q tq db)


  (def all-data-from-1985 '[:find ?title ?year ?genre
                            :where
                            [?e :movie/title ?title]
                            [?e :movie/release-year ?year]
                            [?e :movie/genre ?genre]
                            [?e :movie/release-year 1985]])

  (d/q all-data-from-1985 db)

  (def qr (d/q all-data-from-1985 db))

  (type qr) ;; java.util.HashSet
  (type #{1 2 3}) ;; clojure.lang.PersistentHashSet
  (set? qr) ;; false
  (seq? qr) ;; false
  (coll? qr) ;; false
  (sequential? qr) ;; false

  (seqable? qr) ;; true
  (map identity qr)


  ;;
  )


;; client library

(comment
  (+ 1 2 3)

  ; (require '[datomic.client.api :as d])

  ; (def cfg {:server-type :peer-server
  ;           :access-key "myaccesskey"
  ;           :secret "mysecret"
  ;           :endpoint "localhost:8998"})

  ; (def client (d/client cfg))

  ; (def conn (d/connect client {:db-name "hello"}))

  ;;
  )