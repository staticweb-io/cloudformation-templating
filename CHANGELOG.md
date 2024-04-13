## Unreleased

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
