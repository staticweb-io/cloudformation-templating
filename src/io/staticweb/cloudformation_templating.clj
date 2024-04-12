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

(defn base64
  "The intrinsic function Fn::Base64 returns the Base64 representation
   of the input string. This function is typically used to pass encoded
   data to Amazon EC2 instances by way of the UserData property.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-base64.html"
  [x]
  {"Fn::Base64" x})

(defn cidr
  "The intrinsic function Fn::Cidr returns an array of CIDR address
   blocks. The number of CIDR blocks returned is dependent on the count
   parameter.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-cidr.html"
  [ip-block count cidr-bits]
  {"Fn::Cidr" [ip-block count cidr-bits]})

(defn equals
  "Compares if two values are equal.
   Returns true if the two values are equal or false if they aren't.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-conditions.html#intrinsic-function-reference-conditions-equals"
  [x y]
  {"Fn::Equals" [x y]})

(defn find-in-map
  "The intrinsic function Fn::FindInMap returns the value corresponding
   to keys in a two-level map that's declared in the Mappings section.
   
   The 4-arity version with `default-value` requires the
   AWS::LanguageExtensions transform.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-findinmap.html"
  ([map-name top-level-key second-level-key]
   {"Fn::FindInMap"
    [(full-name map-name)
     (if (integer? top-level-key) top-level-key (full-name top-level-key))
     (if (integer? second-level-key) second-level-key (full-name second-level-key))]})
  ([map-name top-level-key second-level-key default-value]
   (-> (find-in-map map-name top-level-key second-level-key)
       (update "Fn::FindInMap" conj {"DefaultValue" default-value}))))

(defn fn-and
  "Returns true if all the specified conditions evaluate to true,
   or returns false if any one of the conditions evaluates to false.
   Fn::And acts as an AND operator. The minimum number of conditions
   that you can include is 2, and the maximum is 10.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-conditions.html#intrinsic-function-reference-conditions-and"
  [& conds]
  {"Fn::And" (vec conds)})

(defn fn-if
  "Returns one value if the specified condition evaluates to true and
   another value if the specified condition evaluates to false.
   Currently, CloudFormation supports the Fn::If intrinsic function
   in the metadata attribute, update policy attribute, and property
   values in the Resources section and Outputs sections of a template.
   You can use the [[no-value]] pseudo parameter as a return value to
   remove the corresponding property.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-conditions.html#intrinsic-function-reference-conditions-and"
  [cond then else]
  {"Fn::If" [(full-name cond) then else]})

(defn fn-not
  "Returns true for a condition that evaluates to false or returns
   false for a condition that evaluates to true. Fn::Not acts as a
   NOT operator.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-conditions.html#intrinsic-function-reference-conditions-not"
  [cond]
  {"Fn::Not" [cond]})

(defn fn-or
  "Returns true if any one of the specified conditions evaluate to true,
   or returns false if all the conditions evaluates to false.
   Fn::Or acts as an OR operator. The minimum number of conditions that
   you can include is 2, and the maximum is 10.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-conditions.html#intrinsic-function-reference-conditions-or"
  [& conds]
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

(defn get-att
  "The Fn::GetAtt intrinsic function returns the value of an attribute
   from a resource in the template. When the AWS::LanguageExtensions
   transform transform is used, you can use intrinsic functions as
   a parameter to Fn::GetAtt.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html"
  [ref attr]
  {"Fn::GetAtt" [(full-name ref) (full-name attr)]})

(defn get-azs
  "The intrinsic function Fn::GetAZs returns an array that lists
   Availability Zones for a specified Region in alphabetical order.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getavailabilityzones.html"
  [& [region]]
  {"Fn::GetAZs" (or (full-name region) "")})

(defn import-value
  "The intrinsic function Fn::ImportValue returns the value of an output
   exported by another stack. You typically use this function to create
   cross-stack references.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-importvalue.html"
  [name]
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

(defn join
  "The intrinsic function Fn::Join appends a set of values into
   a single value, separated by the specified delimiter. If a delimiter
   is the empty string, the set of values are concatenated with no
   delimiter.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-join.html"
  [separator coll]
  {"Fn::Join" [separator coll]})

(defn length
  "The intrinsic function Fn::Length returns the number of elements
   within an array or an intrinsic function that returns an array.

   Requires the AWS::LanguageExtensions transform.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-length.html"
  [array]
  {"Fn::Length" array})

(def
  ^{:doc
    "Removes the corresponding resource property when specified
     as a return value in the [[fn-if]] intrinsic function.
     
     See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html"}
  no-value {"Ref" "AWS::NoValue"})

(defn not-equals
  "The composition of [[fn-not]] and [[equals]]."
  [x y]
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

(defn ref
  "The intrinsic function Ref returns the value of the specified
   parameter or resource. When the AWS::LanguageExtensions transform
   is used, you can use intrinsic functions as a parameter to Ref.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-ref.html"
  [name]
  {"Ref" (full-name name)})

(def
  ^{:doc
    "Returns a string representing the Region in which the encompassing
     resource is being created, such as us-west-2.
     
     See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html#cfn-pseudo-param-region"}
  region
  {:Ref "AWS::Region"})

(defn select
  "The intrinsic function Fn::Select returns a single object from a
   list of objects by index.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-select.html"
  [index objects]
  {"Fn::Select" [index objects]})

(defn split
  "To split a string into a list of string values so that you can select
   an element from the resulting string list, use the Fn::Split intrinsic
   function. Specify the location of splits with a delimiter, 
   such as , (a comma). After you split a string, use the Fn::Select
   function to pick a specific element.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-split.html"
  [separator s]
  {"Fn::Split" [separator s]})

(def
  ^{:doc
    "Returns the ID of the stack as specified with the
    `aws cloudformation create-stack` command.
     
     See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html#cfn-pseudo-param-stackid"}
  stack-id
  {:Ref "AWS::StackId"})

(def
  ^{:doc
    "Returns the name of the stack as specified with the
    `aws cloudformation create-stack` command.
     
     See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html#cfn-pseudo-param-stackname"}
  stack-name
  {:Ref "AWS::StackName"})

(defn sub
  "The intrinsic function Fn::Sub substitutes variables in an
   input string with values that you specify.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-sub.html"
  [s]
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

(defn transform
  "The intrinsic function Fn::Transform specifies a macro to perform
   custom processing on part of a stack template.
   
   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-transform.html"
  [name parameters]
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
