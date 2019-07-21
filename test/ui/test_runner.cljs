(ns ui.test-runner
  (:require
   ;; [cljs.test :refer-macros [run-tests]]
   ;; [clojure.test :as t] ;; :refer [run-all-tests run-tests]]
   
   [doo.runner :refer-macros [doo-tests doo-all-tests]]

   ;; [arrival-test-task.core]
   [ui.order-model-test]))


(enable-console-print!)

; (defmethod t/report [::t/default :end-run-tests] [m]
;   (prn "end-run-tests")
;   (when-let [exit (cond (exists? js/phantom) js/phantom.exit
;                         (exists? js/process) js/process.exit)]
    
;     (prn "end-run-tests-when-let")
    
;     (exit (+ (:fail m) (:error m)))))



;; https://lambdaisland.com/episodes/testing-clojurescript#/transcript
;; https://8thlight.com/blog/eric-smith/2016/10/05/a-testable-clojurescript-setup.html
;; https://github.com/bensu/doo#setting-up-environments
;; https://hub.packtpub.com/testing-your-application-cljstest/
;; https://lispcast.com/testing-clojurescript/
;; https://figwheel.org/docs/testing.html


;; (run-all-tests)
;; (run-tests 'ui.order-model-test)

;; (doo-all-tests)
;; (doo-tests 'ui.order-model-test 'ui.order-model-test)
(doo-tests 'ui.order-model-test)

(prn "zazaza")

; (when (exists? js/phantom)
;   (js/phantom.exit))

(comment
  (enable-console-print!)
  
  (run-tests 'ui.order-model-test 'ui.order-model-test)

  ;;
  )

;; lein doo chrome-headless test once
;; watch-mode (optional): either auto (default)
;; or once which exits with 0 if the tests were successful and 1 if they failed.


