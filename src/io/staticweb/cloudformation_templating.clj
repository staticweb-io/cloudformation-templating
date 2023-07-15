(ns io.staticweb.cloudformation-templating
  (:require [clojure.string :as str]
            [com.rpl.specter :as sp])
  (:refer-clojure :exclude [ref]))

(defn full-name [x]
  (when x
    (if (string? x)
      x
      (if (simple-ident? x)
        (name x)
        (str (namespace x) "/" (name x))))))

(def account-id
  {:Ref "AWS::AccountId"})

(defn arn [ref]
  {"Fn::GetAtt" [(full-name ref) "Arn"]})

(defn base64 [x]
  {"Fn::Base64" x})

(defn cidr [ip-block count cidr-bits]
  {"Fn::Cidr" [ip-block count cidr-bits]})

(defn equals [x y]
  {"Fn::Equals" [x y]})

(defn find-in-map [map-name top-level-key second-level-key]
  {"Fn::FindInMap" [map-name top-level-key second-level-key]})

(defn fn-and [& conds]
  {"Fn::And" (vec conds)})

(defn fn-if [cond then else]
  {"Fn::If" [(full-name cond) then else]})

(defn fn-not [cond]
  {"Fn::Not" [cond]})

(defn fn-or [& conds]
  {"Fn::Or" (vec conds)})

(defn get-att [ref attr]
  {"Fn::GetAtt" [(full-name ref) attr]})

(defn get-azs [& [region]]
  {"Fn::GetAZs" (or region "")})

(defn import-value [name]
  {"Fn::ImportValue" (full-name name)})

; Logic inferred from
; https://github.com/clojure/clojure/blob/cbb3fdf787a00d3c1443794b97ed7fe4bef8e888/src/jvm/clojure/lang/EdnReader.java#L289
(defn invalid-keyword? [x]
  (and (keyword? x)
    (let [ns (namespace x)
          nm (name x)]
      (or (str/index-of nm "::")
        (and ns
          (or (str/ends-with? ns ":")
            (str/index-of ns "::")))))))

(defn join [separator coll]
  {"Fn::Join" [separator coll]})

(def no-value {"Ref" "AWS::NoValue"})

(defn not-equals [x y]
  (fn-not (equals x y)))

(defn outputs
  "Converts an output map from the format {LogicalId [Name Value Description]}
  to AWS's output format. Description is optional."
  [m]
  (into {}
    (for [[k [name value & [desc]]] m]
      (if (seq desc)
        [k (sorted-map
             :Description desc
             :Value value
             :Export {:Name name})]
        [k (sorted-map
             :Value value
             :Export {:Name name})]))))

(defn prefixed-outputs
  "Converts a map from the format {LogicalId [Value Description]} to AWS's
  output format. Output names are prefixed with the given prefix.

  Example:
  (prefixed-outputs
    \"${AWS::StackName}-\"
    {:VpcId [(ref :Vpc)]})
  ;= {:VpcId {:Export {:Name {\"Fn::Sub\" \"${AWS::StackName}-VpcId\"}}
                       :Value {\"Ref\" \"Vpc\"}}}"
  [prefix m]
  (outputs
    (reduce
      (fn [m [k [value description]]]
        (assoc m k
          [{"Fn::Sub" (str prefix (full-name k))} value description]))
      m
      m)))

(defn ref [name]
  {"Ref" (full-name name)})

(def region
  {:Ref "AWS::Region"})

(defn select [index objects]
  {"Fn::Select" [index objects]})

(defn split [separator s]
  {"Fn::Split" [separator s]})

(def stack-id
  {:Ref "AWS::StackId"})

(def stack-name
  {:Ref "AWS::StackName"})

(defn sub [s]
  {"Fn::Sub" s})

(defn tags [& {:as m}]
  (mapv
    (fn [[k v]]
      {:Key (full-name k) :Value v})
    m))

(defn template [& body]
  (apply sorted-map
    :AWSTemplateFormatVersion "2010-09-09"
    body))

(defn transform [name parameters]
  {"Fn::Transform" {:Name name :Parameters parameters}})

(defn user-data [& data]
  (base64 (join "" data)))

(defmacro deftemplate [name-sym & body]
  `(def ~name-sym
     (template ~@body)))

(defn unsorted-map? [x]
  (and (map? x) (not (sorted? x))))

(defn make-maps-sorted [x]
  (sp/transform (sp/walker unsorted-map?)
    #(make-maps-sorted (into (sorted-map) %))
    x))

(def ^{:doc "This is always the hosted zone ID when you create an alias record (in Route 53) that routes traffic to a CloudFront distribution.

See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-route53-aliastarget.html"}
  cloudfront-hosted-zone-id
  "Z2FDTNDATAQYW2")
