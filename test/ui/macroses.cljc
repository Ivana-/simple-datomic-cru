(ns ui.macroses)

  
(defmacro cond-let
  ([] nil)
  ([x] (throw (IllegalArgumentException. "cond-let error: last one form")))
  ([a b & forms] (if (vector? a)
                   `(let ~a (cond-let ~b ~@forms))
                   `(if ~a ~b (cond-let ~@forms)))))


(defmacro wait-for-fetch-finished
  ([] nil)
  ([a & forms] `(rf-test/wait-for [:fetch-finished] ~@a (wait-for-fetch-finished ~@forms))))


(comment
  (defmacro ma   [x] `(-> '~x clojure.walk/macroexpand-all clojure.pprint/pprint))
  
  (ma (let [x 1 y 2] [x y]))
  
  (ma (wait-for-fetch-finished [a b]))
  
  (ma (wait-for-fetch-finished [a b] [c d e] [f g]))
  
  ;;
  )