## Unreleased

## v3.1.0 (2025-04-24)

- Add a `regions` fn that returns a map of AWS regions and information
  such as the name, domain, location, and partition of each region.
- Add an `elb-data` fn that returns a map of IAM principals for ELB
  logging in each region.

## v3.0.0 (2025-04-17)

- (breaking) Remove invalid-keyword? function.
- (breaking) Remove map sorting fns and specter dependency.
- (breaking) Change :Ref to "Ref" in the return values of several
  functions.
- (breaking) Change various keyword keys to strings in the return values
  of the outputs, template, tags, and transform functions.
  - Templates have a lot of keys that aren't valid keywords,
    like "Fn::If". We previously used a mix of keywords and strings.
    Now all keys are strings all the time, so users that may be
    modifying the results don't have to look up which is which.
    Users may still use any combination of keywords and strings in their
    own code just as before.
- (breaking) Add new and, not, and or functions. Deprecate fn-and, fn-not,
  and fn-or.
- Fix warning about the partition fn shadowing clojure.core/partition.
- Add docstrings for arn, deftemplate, tags, template, and user-data.
- Fix that template fn could not accept a mixture of keyword and
  string keys.

## v2.3.1 (2024-09-19)

- Fix a number of cases where a valid map such as `{"Ref":,,,}` passed to a function resulted in an error. These functions could only accept strings, keywords, and symbols. This has been fixed so that they now accept any valid form.

## v2.3.0 (2024-04-12)

- Add docstring for `account-id`.
- Add the `notification-arns`, `partition`, and `url-suffix` psuedo parameters. See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html

## v2.2.0 (2024-04-11)

- Update to clojure 1.11.2.
- Add [AWS::LanguageExtensions](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/transform-aws-languageextensions.html) functions: `for-each`, `length`, and `to-json-string`. Add optional default-value positional arg to `find-in-map`.
- Add docstrings for many more functions and vars.

## v2.1.0 (2023-08-05)

- Update find-in-map, get-att, and get-azs to turn
  keywords and symbols into strings.

## v2.0.0 (2023-07-14)

- (breaking) Remove `import-from-json`, but add it to the README.
- (breaking) Remove `write-template`.
- Remove dependency on cheshire.

## 1.1.0 (2022-09-12)

* Update to clojure 1.11.1 and cheshire 5.10.1
* Add [Nix](https://nixos.org/) and [direnv](https://direnv.net/) files for development environment
* Add cidr, fn-and, fn-or, get-azs, select, and transform functions
* Add prefixed-outputs and cloudfront-hosted-zone-id

## 0.1.0 (2021-05-09)

* First numbered release
