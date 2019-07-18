(ns ui.macroses)


(defmacro cond-let
  ([] nil)
  ([x] (throw (IllegalArgumentException. "cond-let error: last one form")))
  ([a b & forms] (if (vector? a)
                   `(let ~a (cond-let ~b ~@forms))
                   `(if ~a ~b (cond-let ~@forms)))))

(defmacro <<- [& forms] `(->> ~@(reverse forms)))


(comment
  (defmacro ma   [x] `(-> '~x clojure.walk/macroexpand-all clojure.pprint/pprint))

  (ma (let [x 1 y 2] [x y]))

  (ma (<<-
       (a b 1)
       (c d 2)
       (e f 3)))
  ;;
  )