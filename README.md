# cloudformation-templating

[![Clojars Project](https://img.shields.io/clojars/v/io.staticweb/cloudformation-templating.svg)](https://clojars.org/io.staticweb/cloudformation-templating)

A library for creating CloudFormation templates.

## Importing existing templates

JSON templates can be converted to EDN (or Clojure) using this function:

```clojure
(require '[cheshire.core :as json])
(require '[com.rpl.specter :as sp])

(defn import-from-json [^String s]
  (let [m (json/parse-string s true)]
    (sp/transform (sp/walker invalid-keyword?) full-name m)))
```
