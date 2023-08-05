(ns build
  (:require [org.corfield.build :as bb]))

(def lib 'io.staticweb/cloudformation-templating)
(def version "2.1.0")

(defn get-version [opts]
  (str version (when (:snapshot opts) "-SNAPSHOT")))

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
