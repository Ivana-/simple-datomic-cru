(ns arrival-test-task.config
  (:require
   ;; [config.core :refer [env]]

   [clojure.string :as str]))

(def CONFIG nil)


(defn ne-string [x] (if ((every-pred string? not-empty) x) x ::error))


(def config-structure
  {:datomic-username ne-string
   :datomic-password ne-string
   :datomic-license-key ne-string
   :datomic-storage-admin-password ne-string
   :datomic-storage-datomic-password ne-string
   ;; :zazaza ne-string
   ;;
   })

(defn exit
  ([code template & args]
   (exit code (apply format template args)))

  ([code message]
   (let [out (if (zero? code) *out* *err*)]
     (binding [*out* out]
       (println message)))
   #_(System/exit code)))

(defn coerce-config [config]
  (try
    (let [{:keys [error result]}
          (reduce (fn [acc [k f]]
                    (let [x (get config k)
                          v (f x)]
                      (if (= ::error v)
                        (assoc-in acc [:error k] x)
                        (assoc-in acc [:result k] v))))
                  {} config-structure)]
      (if (empty? error)
        (do
          (println "Config successfully loaded")
          result)
        (exit 1 "Invalid config values: %s %s" \newline
              (str error)
              #_(-> error clojure.pprint/pprint with-out-str))))
    (catch Exception e
      (exit 1 "Wrong config values: %s" (ex-message e)))))


(defn remap-keys [m]
  (reduce (fn [acc [k v]] (assoc acc (-> k
                                         str/lower-case
                                         (str/replace #"_" "-")
                                         keyword) v)) {} m))

(defn parse-content [s type]
  (case type
    :env (->> s
              str/split-lines
              (remove str/blank?)
              (map #(let [[n v] (map str/trim (str/split % #"=" 2))
                          k (if (str/starts-with? n "export ") (str/trim (subs n 7)) n)]
                      [k v]))
              (into {})
              remap-keys)))

(defn read-config-file [filepath filetype]
  (try
    (-> filepath slurp (parse-content filetype))
    (catch Exception e nil)))

(defn set-config! [config] (alter-var-root (var CONFIG) (constantly config)))

(defn load-config! []
  (->> (merge
        (read-config-file ".env_secret" :env)
        (remap-keys (System/getenv)))
       coerce-config
       set-config!))


(comment
  (remap-env (System/getenv))
  (read-config-file ".env_secret" :env)
  ;; (keys env)
  (load-config!)
  CONFIG
  ;;
  )