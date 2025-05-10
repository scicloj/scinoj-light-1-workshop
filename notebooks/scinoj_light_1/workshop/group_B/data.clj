

(ns scinoj-light-1.workshop.group-B.data
  (:require [tablecloth.api :as tc]))


(def raw-projects
  (tc/dataset "data/ks-projects-201801.csv.gz"))


(type raw-projects)

(map? raw-projects)

(keys raw-projects)

(vals raw-projects)

(second (vals raw-projects))

raw-projects


