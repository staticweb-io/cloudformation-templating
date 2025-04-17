(ns io.staticweb.cloudformation-templating-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [io.staticweb.cloudformation-templating :as ct]))

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
