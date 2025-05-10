


(ns scinoj-light-1.workshop.group-B.model
  (:require [scinoj-light-1.workshop.group-B.data :as data]
            [tablecloth.api :as tc]
            [tablecloth.column.api :as tcc]
            [tech.v3.dataset.print :as dsprint]
            [scicloj.kindly.v4.kind :as kind]
            [tech.v3.datatype.datetime :as datetime]
            [scicloj.tableplot.v1.plotly :as plotly]))

(def split-projects
  (-> data/processed-projects
      (tc/drop-rows (fn [row]
                      (= (:state row)
                         "live")))
      (tc/split :holdout
                {:split-names [:train :test]})
      (tc/group-by :$split-name {:result-type :as-map})))

(keys split-projects)

(def train-projects
  (:train split-projects))

(def test-projects
  (:test split-projects))

