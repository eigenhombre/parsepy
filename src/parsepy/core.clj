;; We want to parse Python config files in the form:
;;
;;     [section one]
;;     a = 1
;;     b=3
;;     
;;     [part2]
;;     a="x"
;;     z = 10

;; To accomplish this, we'll use the
;; [Instaparse library](https://github.com/Engelberg/instaparse),
;; which we import as `insta` into our namespace.
;;
;; We'll also set ourselves up to do some
;; [guard-rail
;; programming](http://patrick.lioi.net/2011/11/23/guard-rail-programming/),
;; putting our tests inline with our code to help our description of
;; how this module works.
;;
(ns parsepy.core
  (:require [clojure.test :refer [is]]
            [instaparse.core :as insta]))


;; # Parsing
;; Let's start with recognizing `[section]` tags.  We first declare
;; our grammar, in something like
;; [BNF](http://en.wikipedia.org/wiki/Backus%E2%80%93Naur_Form).
(def sections
  (insta/parser
   "body = newline* section-tag+
    section-tag = lbrace terms rbrace newline+
    <lbrace> = <'['>
    <rbrace> = <']'>
    <newline> = <'\n'>
    <terms> = #'[a-zA-Z0-9\\s]+'"))

;; This produces output that's easily manipulated with Clojure
;; functions; namely, nested vectors.
(is (= (sections "[My Section 1]\n")
       [:body [:section-tag "My Section 1"]]))

;; We also make sure multiple terms work.
(is (= (sections "[a]\n\n\n[b]\n")
       [:body [:section-tag "a"]
              [:section-tag "b"]]))

;; Let's add assignment statements in the form `x=5`, and comments, in
;; the form of
;;
;;     # This is a comment
;;
;; Our grammar becomes more complicated, to handle multiple sections,
;; each with (potentially) multiple assignments and comments.
;;
(def parser
  (insta/parser
   "<body> = <newline*> section+
    section = <comment*> section-tag (<comment> | assignment)*
    <section-tag> = <space>* <lbrace> section-terms <rbrace> newline+
    <comment> = <hash> #'.+?\\n+'
    <hash> = '#'
    section-terms = s0 st*
    <s0> = #'[a-zA-Z_]'
    <st> = (space | #'[a-zA-Z0-9_]+')
    <assignment> = lvalue <space*> <equal> <space>* const newline+
    <space> = ' '
    equal = <'='>
    lbrace = <'['>
    rbrace = <']'>
    <newline> = <'\n'>
    <const> = #'\\S+'
    lvalue = #'[a-zA-Z][a-zA-Z0-9_]*'"))

;; We also tranform our parsed data into something more natural
;; for consumption by other Clojure functions, by turning any
;; left-hand-side values into keywords, and concatenating the strings
;; that make up any section titles.
;;
(def transform-options {:lvalue keyword
                        :section-terms (partial apply str)})

;; Our parsing function then is just
(defn parse [input]
  (->> (parser input)
       (insta/transform transform-options)))

;; and it works on both single sections
(is (= (parse "[a]\nb=1\n")
       [[:section "a" :b "1"]]))

;; and multiple sections.
(is (= (parse "[a section]

# A comment

y = 999
z = torNado_3


 [b]
q=10
")
    [[:section "a section" :y "999" :z "torNado_3"]
     [:section "b" :q "10"]]))

;; Parsing config files is now very straightforward.
(comment
  (clojure.pprint/pprint
   (parse (slurp "/Users/jacobsen/Dropbox/icecube/live/config/conf/defaults.conf")))

  ==>

  ([:section
  "log"
  :log_location
  "$HOME/.i3live.log"
  :max_log_mb
  "100"
  :max_log_files
  "7"]
 [:section
  "dbserver"
  :loglevel
  "info"
  :port
  "7000"
  :cachedir
  "/mnt/data/i3live"
  :cachesize
  "1000000000"
  :jsonport
  "7002"
  :jsonfile
  "$HOME/catchall.json"]
  [:section "filewatcher" :loglevel "info"]
  ;; ...   
  )
)

;; We could do more with this in terms of getting nicer data
;; structures, or handling multi-line assignments, but this module
;; already does most of what Python's
;; [ConfigParser](http://docs.python.org/2/library/configparser.html)
;; does, with relatively little code.
;;
;; The package is up on Clojars and [GitHub](https://github.com/eigenhombre/parsepy).
