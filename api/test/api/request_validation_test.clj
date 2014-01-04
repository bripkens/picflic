(ns api.request-validation-test
  (:require [cheshire.core :refer [generate-string]])
  (:use clojure.test
        ring.mock.request
        api.request-validation))

(deftest test-basic-validation
  (is (:success (validate "collection" (generate-string {:_id "123"})))))

(deftest test-missing-required-properties
  (is (not (:success (validate "collection" (generate-string {}))))))

(deftest test-sub-resource-missing-property
  (let [data {:_id "123"
              :name "foo"
              :images [{:blub "bla"}]}]
    (is (not (:success (validate "collection" (generate-string data)))))))

(deftest test-too-many-properties
  (let [data {:name "foo" :bla false}]
    (is (not (:success (validate "collection" (generate-string data)))))))

(deftest test-check-content-type
  (let [req (fn [method content-type]
              {:request {:request-method method
               :headers {"content-type" content-type}}})
        unsupported (fn [result] (not (true? result)))
        supported #(not (seq? %))
        json "application/json"
        xml "application/xml"]
    ; should ignore get, delete and patch
    (is (supported (check-content-type (req :get xml) [json])))
    (is (supported (check-content-type (req :delete xml) [json])))
    (is (supported (check-content-type (req :patch xml) [json])))

    ; wrong content type
    (is (unsupported (check-content-type (req :put xml) [json])))
    (is (unsupported (check-content-type (req :post xml) [json])))

    ; correct content type
    (is (supported (check-content-type (req :put json) [json])))
    (is (supported (check-content-type (req :post json) [json])))
    (is (supported (check-content-type (req :put xml) [json xml])))
    (is (supported (check-content-type (req :post xml) [json xml])))))
