(ns io.staticweb.cloudformation-templating-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [io.staticweb.cloudformation-templating :as ct]))

(deftest test-elb-data
  (let [test-keys [:account-arn :account-id :service-principal]]
    (testing "elbs contains account ID and ARN for older regions"
      (is (= {:account-arn "arn:aws:iam::033677994240:root"}
             (select-keys (:us-east-2 (ct/elb-data)) test-keys))))
    (testing "elbs contains service principal for newer regions"
      (is (= {:service-principal "logdelivery.elasticloadbalancing.amazonaws.com"}
             (select-keys (:ap-southeast-4 (ct/elb-data)) test-keys))))))

(deftest test-find-in-map
  (testing "find-in-map emits the correct syntax")
  (is (= {"Fn::FindInMap" ["MyMap" "A" "B"]}
        (ct/find-in-map :MyMap :A :B))
      "without default value")
  (is (= {"Fn::FindInMap" ["MyMap" "A" "B" {"DefaultValue" 0}]}
         (ct/find-in-map :MyMap :A :B 0))
      "with default value")
  (is (= {"Fn::FindInMap" ["MyMap" {"Ref" "AWS::Region"} "B" {"DefaultValue" 0}]}
         (ct/find-in-map :MyMap {"Ref" "AWS::Region"} :B 0))
      "with a ref map"))

; From https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-foreach-example-resource.html
(def for-each-topics-example
  {"Fn::ForEach::Topics"
   ["TopicName"
    {"Ref" "pRepoARNs"}
    {"SnsTopic${TopicName}"
     {"Type" "AWS::SNS::Topic"
      "Properties"
      {"TopicName"
       {"Fn::Join"
        ["."
         [{"Ref" "TopicName"}
          "fifo"]]}
       "FifoTopic" true}}}]})

(deftest test-for-each
  (testing "for-each emits the correct syntax"
    (is (= for-each-topics-example
           (ct/for-each
             :Topics
             :TopicName
             (ct/ref :pRepoARNs)
             "SnsTopic${TopicName}"
             {"Type" "AWS::SNS::Topic"
              "Properties"
              {"TopicName"
               (ct/join "." [(ct/ref :TopicName) "fifo"])
               "FifoTopic" true}}))
        "with keyword identifiers")
    (is (= for-each-topics-example
           (ct/for-each
             "Topics"
             "TopicName"
             (ct/ref :pRepoARNs)
             "SnsTopic${TopicName}"
             {"Type" "AWS::SNS::Topic"
              "Properties"
              {"TopicName"
               (ct/join "." [(ct/ref :TopicName) "fifo"])
               "FifoTopic" true}}))
        "with string identifiers")))

(deftest test-regions
  (let [test-keys
        #__ [:code :domain :geolocation-country :geolocation-region
             :long-name :opt-in? :partition]]
    (testing "regions contains basic data about each AWS region"
      (is (= {:code "us-east-2"
              :domain "amazonaws.com"
              :geolocation-country "US"
              :geolocation-region "US-OH"
              :long-name "US East (Ohio)"
              :opt-in? false
              :partition "aws"}
             (select-keys (:us-east-2 (ct/regions)) test-keys)))
      (is (= {:code "ap-northeast-3"
              :domain "amazonaws.com"
              :geolocation-country "JP"
              :geolocation-region "JP-27"
              :long-name "Asia Pacific (Osaka)"
              :opt-in? false
              :partition "aws"}
             (select-keys (:ap-northeast-3 (ct/regions)) test-keys))))))

(deftest test-template
  (testing "template works with both keyword and string keys"
    (is (= {"AWSTemplateFormatVersion" "2010-09-09"
            "Resources" {}}
           (ct/template "Resources" {})))
    (is (= {"AWSTemplateFormatVersion" "2010-09-09"
            :Resources {}}
          (ct/template :Resources {}))))
  (testing "AWSTemplateFormatVersion can be overridden"
    (is (= {"AWSTemplateFormatVersion" "2025-04-17"}
           (ct/template "AWSTemplateFormatVersion" "2025-04-17")))
    (is (= {:AWSTemplateFormatVersion "2025-04-17"}
           (ct/template :AWSTemplateFormatVersion "2025-04-17")))))
