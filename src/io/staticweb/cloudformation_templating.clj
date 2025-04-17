(ns io.staticweb.cloudformation-templating
  (:refer-clojure :exclude [and not or partition ref]))

(defn full-name
  "For keywords and symbols, returns the namespace and name of
   the ident. For strings, returns the string unchanged."
  [x]
  (when x
    (if (string? x)
      x
      (if (simple-ident? x)
        (name x)
        (str (namespace x) "/" (name x))))))

(defn full-name-if-ident
  "For keywords and symbols, returns the namespace and name of the
   ident. For other values, returns the value unchanged."
  [x]
  (if (ident? x)
    (full-name x)
    x))

(defn- compare-full-names
  [a b]
  (compare (full-name a) (full-name b)))

(defn- sorted-map-by-full-name
  [& keyvals]
  (apply sorted-map-by
    compare-full-names
    keyvals))

(def
  ^{:doc
    "Returns the AWS account ID of the account in which the stack
     is being created.

     See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html#cfn-pseudo-param-accountid"}
  account-id
  {"Ref" "AWS::AccountId"})

(defn and
  "Returns true if all the specified conditions evaluate to true,
   or returns false if any one of the conditions evaluates to false.
   Fn::And acts as an AND operator. The minimum number of conditions
   that you can include is 2, and the maximum is 10.

   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-conditions.html#intrinsic-function-reference-conditions-and"
  [& conds]
  {"Fn::And" (vec conds)})

(defn ^{:deprecated "3.0"} fn-and
  "Deprecated: Use [[and]].
   
   Returns true if all the specified conditions evaluate to true,
   or returns false if any one of the conditions evaluates to false.
   Fn::And acts as an AND operator. The minimum number of conditions
   that you can include is 2, and the maximum is 10.

   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-conditions.html#intrinsic-function-reference-conditions-and"
  [& conds]
  (apply and conds))

(defn arn
  "Returns a template function map to get the ARN of a resource.

   Only works for resources that support \"Fn::GetAtt\" to get the ARN.
   Some resources require \"Ref\" to get the ARN, so you
   should use [[ref]] instead for those."
  [ref]
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
    [(full-name-if-ident map-name)
     (full-name-if-ident top-level-key)
     (full-name-if-ident second-level-key)]})
  ([map-name top-level-key second-level-key default-value]
   (-> (find-in-map map-name top-level-key second-level-key)
       (update "Fn::FindInMap" conj {"DefaultValue" default-value}))))

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
  {"Fn::If"
   [(full-name-if-ident cond) then else]})

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
  {(str "Fn::ForEach::" (full-name-if-ident loop-name))
   [(full-name-if-ident id) coll {(full-name-if-ident output-key) output-val}]})

(defn get-att
  "The Fn::GetAtt intrinsic function returns the value of an attribute
   from a resource in the template. When the AWS::LanguageExtensions
   transform transform is used, you can use intrinsic functions as
   a parameter to Fn::GetAtt.

   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html"
  [ref attr]
  {"Fn::GetAtt" [(full-name-if-ident ref) (full-name-if-ident attr)]})

(defn get-azs
  "The intrinsic function Fn::GetAZs returns an array that lists
   Availability Zones for a specified Region in alphabetical order.

   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getavailabilityzones.html"
  [& [region]]
  {"Fn::GetAZs" (clojure.core/or (full-name-if-ident region) "")})

(defn import-value
  "The intrinsic function Fn::ImportValue returns the value of an output
   exported by another stack. You typically use this function to create
   cross-stack references.

   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-importvalue.html"
  [name]
  {"Fn::ImportValue" (full-name-if-ident name)})

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
  no-value
  {"Ref" "AWS::NoValue"})

(defn not
  "Returns true for a condition that evaluates to false or returns
   false for a condition that evaluates to true. Fn::Not acts as a
   NOT operator.

   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-conditions.html#intrinsic-function-reference-conditions-not"
  [cond]
  {"Fn::Not" [cond]})

(defn ^{:deprecated "3.0"} fn-not
  "Deprecated: Use [[not]].
   
   Returns true for a condition that evaluates to false or returns
   false for a condition that evaluates to true. Fn::Not acts as a
   NOT operator.

   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-conditions.html#intrinsic-function-reference-conditions-not"
  [cond]
  (not cond))

(defn not-equals
  "The composition of [[not]] and [[equals]]."
  [x y]
  (not (equals x y)))

(def
  ^{:doc
    "Returns the list of notification Amazon Resource Names (ARNs)
     for the current stack.

     To get a single ARN from the list, use [[select]].

     See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html#cfn-pseudo-param-notificationarns"}
  notification-arns
  {"Ref" "AWS::NotificationARNs"})

(defn or
  "Returns true if any one of the specified conditions evaluate to true,
   or returns false if all the conditions evaluates to false.
   Fn::Or acts as an OR operator. The minimum number of conditions that
   you can include is 2, and the maximum is 10.

   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-conditions.html#intrinsic-function-reference-conditions-or"
  [& conds]
  {"Fn::Or" (vec conds)})

(defn ^{:deprecated "3.0"} fn-or
  "Deprecated: Use [[or]].
   
   Returns true if any one of the specified conditions evaluate to true,
   or returns false if all the conditions evaluates to false.
   Fn::Or acts as an OR operator. The minimum number of conditions that
   you can include is 2, and the maximum is 10.

   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-conditions.html#intrinsic-function-reference-conditions-or"
  [& conds]
  (apply or conds))

(defn outputs
  "Converts an output map from the format {LogicalId [Name Value Description]}
  to AWS's output format. Description is optional."
  [m]
  (into {}
    (for [[k [name value & [desc]]] m]
      (if (seq desc)
        [k (sorted-map-by-full-name
             "Description" desc
             "Value" value
             "Export" {"Name" name})]
        [k (sorted-map-by-full-name
             "Value" value
             "Export" {"Name" name})]))))

(def
  ^{:doc
    "Returns the partition that the resource is in. For standard
     AWS Regions, the partition is aws. For resources in other
     partitions, the partition is aws-partitionname. For example,
     the partition for resources in the China (Beijing and Ningxia)
     Region is `aws-cn` and the partition for resources in the
     AWS GovCloud (US-West) region is `aws-us-gov`.

     See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html#cfn-pseudo-param-partition"}
  partition
  {"Ref" "AWS::Partition"})

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
          [{"Fn::Sub" (str prefix (full-name-if-ident k))} value description]))
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
  {"Ref" "AWS::Region"})

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
  {"Ref" "AWS::StackId"})

(def
  ^{:doc
    "Returns the name of the stack as specified with the
    `aws cloudformation create-stack` command.

     See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html#cfn-pseudo-param-stackname"}
  stack-name
  {"Ref" "AWS::StackName"})

(defn sub
  "The intrinsic function Fn::Sub substitutes variables in an
   input string with values that you specify.

   See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-sub.html"
  [s]
  {"Fn::Sub" s})

(defn tags
  "Converts a map from the format {k v ...}
   to AWS's tags format [{\"Key\" k \"Value\" v ...}].

   Keys may be keywords or strings."
  [& {:as m}]
  (mapv
    (fn [[k v]]
      {"Key" (full-name-if-ident k) "Value" v})
    m))

(defn template
  "Returns a [[sorted-map]] of the body with
   \"AWSTemplateFormatVersion\" added."
  [& body]
  (apply sorted-map-by-full-name
         "AWSTemplateFormatVersion" "2010-09-09"
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
  {"Fn::Transform" {"Name" name "Parameters" parameters}})

(def
  ^{:doc
    "Returns the suffix for a domain. The suffix is typically
     `amazonaws.com`, but might differ by Region. For example,
     the suffix for the China (Beijing) Region is `amazonaws.com.cn`.

     See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html#cfn-pseudo-param-urlsuffix"}
  url-suffix
  {"Ref" "AWS::URLSuffix"})

(defn user-data
  "Returns a template function map that concatenates the data strings
   and base64-encodes the result.

   Equivalent to {\"Fn::Base64\" {\"Fn::Join\" [\"\", data]}}."
  [& data]
  (base64 (join "" data)))

(defmacro deftemplate
  "Defines a template var.

   Equivalent to (def name-sym (template body))."
  [name-sym & body]
  `(def ~name-sym
     (template ~@body)))

(def ^{:doc "This is always the hosted zone ID when you create an alias record (in Route 53) that routes traffic to a CloudFront distribution.

See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-route53-aliastarget.html"}
  cloudfront-hosted-zone-id
  "Z2FDTNDATAQYW2")
