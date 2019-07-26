(ns arrival-test-task.config)

(def debug? ^boolean goog.DEBUG)

;; (goog-define test? false) ;; redefines in "test" build config :cljsbuild {:builds {:closure-defines

(def      backend-url "http://localhost:3000")
(def test-backend-url "http://localhost:3001")

(comment
  
  ;; https://www.martinklepsch.org/posts/parameterizing-clojurescript-builds.html
  
  goog.DEBUG ;; true by default
  ;; test? ;; false by default
  
  )