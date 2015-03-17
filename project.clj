(defproject jdbcrepl "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [com.oracle/ojdbc6 "1.0"]
                 [com.microsoft/sqljdbc4 "1.0"]
                 [org.clojure/data.csv "0.1.2"]
                 [clj-http "1.0.1"]
                 [org.clojure/core.incubator "0.1.3"]]
  :main ^:skip-aot jdbcrepl.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :repositories {"sesam-build" "http://sesam-build.cloudapp.net:8081/artifactory/ext-release-local"})
