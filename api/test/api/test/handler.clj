(ns api.test.handler
  (:require [cheshire.core :refer [generate-string]])
  (:use clojure.test
        ring.mock.request  
        api.request-validation))

; (deftest test-app
;   (testing "main route"
;     (let [response (app (request :get "/"))]
;       (is (= (:status response) 200))
;       (is (= (:body response) "Hello World"))))
  
;   (testing "not-found route"
;     (let [response (app (request :get "/invalid"))]
;       (is (= (:status response) 404)))))



(deftest test-basic-validation
  (is (:success (validate "collection" (generate-string {:name "Foobar"})))))

(deftest test-missing-required-properties
  (is (not (:success (validate "collection" (generate-string {}))))))

(deftest test-sub-resource-missing-property
  (let [data {:name "foo"
              :images [{:blub "bla"}]}]
    (is (not (:success (validate "collection" (generate-string data)))))))
