


(ns scinoj-light-1.workshop.group-A.model
  (:require [tablecloth.api :as tc]
            [tablecloth.column.api :as tcc]
            [scinoj-light-1.workshop.group-A.data :as data]
            [tech.v3.dataset.print :as dsprint]
            [tech.v3.datatype.datetime :as datetime]
            [scicloj.tableplot.v1.plotly :as plotly]
            [fastmath.ml.regression :as fmreg]))


;; logistic regression - a statistical algorithm for classification

;; a one-layer neural network

;; but with clearer interpretations


(def data-for-prediction 
  (-> data/clean-projects
      (tc/map-columns :theater
                      [:main_category]
                      (fn [main-category]
                        (if (= main-category "Theater")
                          1 0)))
      (tc/map-columns :target
                      [:state]
                      (fn [state]
                        (if (= state "successful")
                          1 0)))
      (tc/select-columns [:log10goal :theater :target])))

(def split-data
  (-> data-for-prediction
      (tc/split :holdout
                {:split-names [:train :test]})
      (tc/group-by :$split-name {:result-type :as-map})))

(keys split-data)

(def train-data (:train split-data))
(def test-data (:test split-data))

train-data

test-data

;; the probability of success:

;; sigmoid (A * log10goal + B * theater)

(def model
  (fmreg/glm
   ;; ys
   (:target train-data)
   ;; xss
   (-> train-data
       (tc/select-columns [:log10goal :theater])
       tc/rows)
   ;; options
   {:family :binomial
    :tol 0.5}))


(-> test-data
    (tc/add-column :probability
                   (fn [ds]
                     (-> ds
                         (tc/select-columns [:log10goal :theater])
                         tc/rows
                         (->> (map model)))))
    (tc/add-column :prediction
                   (fn [ds]
                     (tcc/> (:probability ds) 0.5)))
    (tc/group-by [:prediction])
    (tc/aggregate {:actual-rate-of-success
                   (fn [ds]
                     (tcc/mean (:target ds)))}))


