


(ns scinoj-light-1.workshop.group-B.model
  (:require [scinoj-light-1.workshop.group-B.data :as data]
            [tablecloth.api :as tc]
            [tablecloth.column.api :as tcc]
            [tech.v3.dataset.print :as dsprint]
            [scicloj.kindly.v4.kind :as kind]
            [tech.v3.datatype.datetime :as datetime]
            [scicloj.tableplot.v1.plotly :as plotly]
            [fastmath.ml.regression :as fmreg]))

(def split-projects
  (-> data/processed-projects
      (tc/drop-rows (fn [row]
                      (= (:state row)
                         "live")))
      (tc/map-columns :successful
                      [:state]
                      (fn [state]
                        (if (= state "successful")
                          1 0)))
      (tc/map-columns :theater
                      [:main_category]
                      (fn [main-category]
                        (if (= main-category "Theater")
                          1 0)))
      (tc/split :holdout {:split-names [:train :test]
                          :seed 1})
      (tc/group-by :$split-name {:result-type :as-map})))

(keys split-projects)

(def train-projects
  (:train split-projects))

(def test-projects
  (:test split-projects))

;; regression - we train to predict a number
;; for example: linear regression

;; classification - we train to predict a category
;; for example: logistic regression


(-> train-projects
    :theater
    frequencies)

(-> train-projects
    (tc/select-columns [:theater :successful]))


;; sigmoid(A + B * theater + C * ...) = probability of success

(:successful train-projects)

(-> train-projects
    (tc/select-columns [:theater])
    tc/rows)

(def model
  (fmreg/glm (:successful train-projects)
             (-> train-projects
                 (tc/select-columns [:theater])
                 tc/rows)
             {:family :binomial}))

(keys model)

(:coefficients model)

(model [0])
(model [1])

(-> test-projects
    (tc/add-column :probability
                   (fn [ds]
                     (map model
                          (-> ds
                              (tc/select-columns [:theater])
                              tc/rows))))
    (tc/add-column :prediction
                   (fn [ds]
                     (tcc/> (:probability ds) 0.5)))
    (tc/group-by [:prediction])
    (tc/aggregate {:n tc/row-count
                   :rate-of-success (fn [ds]
                                      (-> ds
                                          :state
                                          (tcc/eq "successful")
                                          tcc/mean))}))
