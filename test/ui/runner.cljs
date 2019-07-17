(ns ui.runner
  (:require
   [cljs.test :refer-macros [run-tests]]
   [ui.order-model-test]))

;; (run-tests 'ui.tcljs 'ui.tcljs)

(comment
  (enable-console-print!)
  
  (run-tests 'ui.order-model-test 'ui.order-model-test)

  ;;
  )