(ns shurly.data
  (:require [datomic.api :as d]
            [environ.core :refer [env]]))

(def uri (env :datomic-url))

(def schemas [[{:db/id #db/id[:db.part/db]
                :db/ident :rule/url
                :db/valueType :db.type/string
                :db/cardinality :db.cardinality/one
                :db/doc "The destination URL of a redirect rule"
                :db.install/_attribute :db.part/db}]
              [{:db/id #db/id[:db.part/db]
                :db/ident :rule/slug
                :db/valueType :db.type/string
                :db/cardinality :db.cardinality/one
                :db/doc "The slug for the redirect rule"
                :db.install/_attribute :db.part/db}]])

;; Create database and define schema
(defn create []
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (map #(d/transact conn %1) schemas)))

(defn delete []
  (d/delete-database uri))

(defn recreate []
  (delete)
  (create))

(defn conn []
  (d/connect uri))

(defn db []
  (d/db (conn)))

;; Wrapper for the query interface
(def q #(d/q % (db)))
(def t #(d/transact (conn) %))

