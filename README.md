# parsepy

A Clojure module for parsing Python configuration files.

### Author

[John Jacobsen](http://eigenhombre.com)

### Dependencies

Uses the truly awesome [Instaparse](https://github.com/Engelberg/instaparse) library.

See the [Marginalia
docs](http://eigenhombre.com/semi-literate-programming/parsepy.html)
for more documentation.

### Usage

Add to `project.clj`:

    [parsepy "0.0.1"]

And import `parsepy.core`.

Example:

    (ns my.great.namespace
      (require [parsepy.core :as parsepy]))

    (parsepy/parse "

    # This is a Python configuration file, suitable for parsing 
    # with ConfigParser or parsepy.
    
    [section_one]
    x = 1
    s = string_value
    ")

    ;;=> ([:section section_one :x 1 :s string_value])

## Limitations:

Does not handle quoted or multiline strings yet, variable
interpolation, or `allow_no_value` options. I'll implement them when I
need to; pull requests welcome in the mean time.
