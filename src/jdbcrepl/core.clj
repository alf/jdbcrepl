(ns jdbcrepl.core
  (:require [clojure.core.strint :refer [<<]]
            [clojure.java.jdbc :as j]
            [clojure.data.csv :as csv]
            [clj-http.client :as client]))

(defn replace-template [text m]
  (clojure.string/replace text #"\{\w+\}"
                          (comp m keyword clojure.string/join butlast rest)))

(defn seqmap->org-table
  "Converts a sequence of maps to an org-mode table."
  [s]
  (reduce (fn [a m]
            (let [sm (sort m)
                  vs (map str (vals sm))]
              (if (empty? a)
                (vector (map str (keys sm)) 'hline vs)
                (conj a vs))))
          [] s))

(defn org-table->seqmap
  "Converts an org-mode table to a sequence of maps."
  [t]
  (let [headers (map keyword (first t))
        rows (filter #(not (= 'hline %)) (rest t))]
    (reduce (fn [a r]
              (conj a (zipmap headers r)))
            [] rows)))

(defn org-jdbc-query
  "Runs an sql query and returns the result as an org-mode table."
  ([db-spec query]
   (j/with-db-connection [db-con db-spec]
     (seqmap->org-table (j/query db-con query))))
  ([db-spec query org-table]
   (j/with-db-connection [db-con db-spec]
     (seqmap->org-table
      (mapcat #(j/query db-con %)
           (map (partial replace-template query)
                (org-table->seqmap org-table)))))))

(defn org-jdbc-get-tables
  "Returns the database schema as an org-mode table."
  ([db-spec] (org-jdbc-get-tables db-spec nil nil nil nil))
  ([db-spec schemaPattern] (org-jdbc-get-tables db-spec nil schemaPattern nil nil))
  ([db-spec catalog schemaPattern tableNamePattern types]
   (seqmap->org-table
    (j/with-db-metadata [meta db-spec]
      (doall (resultset-seq (.getTables meta catalog schemaPattern tableNamePattern types)))))))

(defn org-jdbc-get-columns
  "Returns the database schema as an org-mode table."
  ([db-spec] (org-jdbc-get-columns db-spec nil nil nil nil))
  ([db-spec tableNamePattern] (org-jdbc-get-columns db-spec nil nil tableNamePattern nil))
  ([db-spec schemaPattern tableNamePattern] (org-jdbc-get-columns db-spec nil schemaPattern tableNamePattern nil))
  ([db-spec catalog schemaPattern tableNamePattern columnNamePattern]
   (seqmap->org-table
    (j/with-db-metadata [meta db-spec]
      (doall (resultset-seq (.getColumns meta catalog schemaPattern tableNamePattern columnNamePattern)))))))

(defn- sparql-get [url query]
  (let [resp
        (client/post url
                     {:form-params {:query query}
                      :socket-timeout 30000
                      :conn-timeout 1000})]))

(defn sparql-query
  "Runs a sparql query and returns the result as an org-mode table."
  ([query] (sparql-query "http://localhost:8890/sparql" query))
  ([endpoint query]
   (sparql-get endpoint query)))

(defn enable-socks-proxy
  ([port] (enable-socks-proxy "localhost" port))
  ([host port]
   (System/setProperty "socksProxyHost", host)
   (System/setProperty "socksProxyPort", port)
   true))

(defn set-dns-server
  [host] (System/setProperty "sun.net.spi.nameservice.nameservers" host))

(defn unset-dns-server
  [] (set-dns-server ""))

(defn disable-socks-proxy
  []
  (enable-socks-proxy "" ""))
