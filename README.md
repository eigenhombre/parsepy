# parsepy

A Clojure module for parsing Python configuration files.

## Author

[John Jacobsen](http://eigenhombre.com)

## Usage

    ;; add to `project.clj`: [parsepy "0.0.1"]

    (ns my.great.namespace
      (require [parsepy.core :as parsepy]))

    (parse "

    # This is a Python configuration file, suitable for parsing 
    # with ConfigParser or parsepy.
    [section_one]
    x = 1
    s = "A really great string"


