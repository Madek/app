(ns madek.app.server.export.meta-data-schema
  (:require
    [madek.app.server.state :as state]
    [json-roa.client.core :as roa]
    [madek.app.server.utils :refer [deep-merge]]

    [cheshire.core :as cheshire]
    [clojure.java.io :as io]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.catcher :as catcher]
    [logbug.thrown :as thrown]
    [logbug.debug :as debug :refer [I> I>>]]
    )

  (:import
    [java.io File]
    ))

(defn meta-keys [vocabulary]
  (->> (-> vocabulary
           (roa/relation :meta-keys)
           (roa/get {})
           roa/coll-seq)
       (map #(roa/get % {}))
       (map roa/data)
       (sort-by :position)))

(defn vocablulary-with-meta-keys [vocabulary]
  (assoc (roa/data vocabulary)
         :meta-keys (meta-keys vocabulary)))

(defn meta-data-schema_unmemoized []
  {:vocabularies
   (->> (-> (roa/get-root (state/connection-entry-point)
                          :default-conn-opts (state/connection-http-options))
            (roa/relation :vocabularies)
            (roa/get {})
            roa/coll-seq)
        (map #(roa/get % {}))
        (map vocablulary-with-meta-keys)
        (sort-by :position))})

(def meta-data-schema (memoize meta-data-schema_unmemoized))

(defn download [target-dir]
  (let [path (str target-dir File/separator "meta-data_schema.json")
        schema (meta-data-schema)]
    (io/make-parents path)
    (spit path (cheshire/generate-string schema {:pretty true}))))


;### Debug ####################################################################
;(debug/re-apply-last-argument #'meta-data-schema_unmemoized)
;(debug/re-apply-last-argument #'download)
;(debug/debug-ns *ns*)

