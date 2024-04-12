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

(defn find-in-map
  ([map-name top-level-key second-level-key]
   {"Fn::FindInMap"
    [(full-name map-name)
     (if (integer? top-level-key) top-level-key (full-name top-level-key))
     (if (integer? second-level-key) second-level-key (full-name second-level-key))]})
  ([map-name top-level-key second-level-key default-value]
   (-> (find-in-map map-name top-level-key second-level-key)
       (update "Fn::FindInMap" conj {"DefaultValue" default-value}))))

(defn fn-and [& conds]
  {"Fn::And" (vec conds)})

(defn fn-if [cond then else]
  {"Fn::If" [(full-name cond) then else]})

(defn fn-not [cond]
  {"Fn::Not" [cond]})

(defn fn-or [& conds]
  {"Fn::Or" (vec conds)})

(defn for-each
  "The Fn::ForEach intrinsic function takes a collection and a fragment,
   and applies the items in the collection to the identifier
   in the provided fragment.

   Requires the AWS::LanguageExtensions transform.

   `loop-name` must be globally unique within the template.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-foreach.html
   
   Example:

   ```
   (ct/for-each
     :Topics
     :TopicName
     (ct/ref :pRepoARNs)
     \"SnsTopic${TopicName}\"
     {\"Type\" \"AWS::SNS::Topic\"
         \"Properties\"
         {\"TopicName\"
         (ct/join \".\" [(ct/ref :TopicName) \"fifo\"])
         \"FifoTopic\" true}})
   ```"
  [loop-name id coll output-key output-val]
  {(str "Fn::ForEach::" (full-name loop-name))
   [(full-name id) coll {(full-name output-key) output-val}]})

(defn get-att [ref attr]
  {"Fn::GetAtt" [(full-name ref) (full-name attr)]})

(defn get-azs [& [region]]
  {"Fn::GetAZs" (or (full-name region) "")})

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

(defn length
  "The intrinsic function Fn::Length returns the number of elements
   within an array or an intrinsic function that returns an array.

   Requires the AWS::LanguageExtensions transform.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-length.html"
  [array]
  {"Fn::Length" array})

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

(defn to-json-string
  "The Fn::ToJsonString intrinsic function converts an object or array
   to its corresponding JSON string.

   Requires the AWS::LanguageExtensions transform.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-ToJsonString.html"
  [object-or-array]
  {"Fn::ToJsonString" object-or-array})

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
