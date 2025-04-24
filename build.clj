(ns build
  (:require
   [clojure.data.json :as json]
   [clojure.java.process :as p]
   [clojure.pprint :as pp]
   [org.corfield.build :as bb]))

(def lib 'io.staticweb/cloudformation-templating)
(def version "3.0.0")

(defn get-version [opts]
  (str version (when (:snapshot opts) "-SNAPSHOT")))

(defn region-data [{:keys [RegionName RegionOptStatus]}]
  {:code RegionName
   :opt-in? (not= "ENABLED_BY_DEFAULT" RegionOptStatus)})

(defn update-regions "Update regions.edn" [_opts]
  (let [regions (-> (p/exec
                     {:err :inherit}
                     "aws" "account" "list-regions" "--output" "json")
                    (json/read-str :key-fn keyword)
                    :Regions
                    (->> (map region-data)
                         (reduce
                          (fn [acc {:as m :keys [code]}]
                            (assoc acc (keyword code) m))
                          (sorted-map))))]
    (spit "resources/io/staticweb/cloudformation-templating/regions.edn"
          (with-out-str (pp/pprint regions))))
  (p/exec
   {:err :inherit :out :inherit}
   "bin/format"))

(defn jar "Build the JAR." [opts]
  (-> opts
      (assoc :lib lib :version (get-version opts))
      bb/clean
      (assoc :src-pom "template/pom.xml")
      bb/jar))

(defn deploy "Deploy the JAR to Clojars." [opts]
  (-> opts
      (assoc :lib lib :version (get-version opts))
      bb/deploy))
