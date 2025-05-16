;; # Intro to Noj

;; https://www.kaggle.com/datasets/kemical/kickstarter-projects

;; https://scicloj.github.io/noj/

;; ## Setup

(ns scinoj-light-1.workshop.start.walkthrough
  (:require [tablecloth.api :as tc]
            [clojure.string :as str]
            [clojure.math :as math]
            [java-time.api :as java-time]
            [tech.v3.datatype.datetime :as datetime]
            [tech.v3.dataset.print :as dsprint]
            [tech.v3.datatype.datetime :as datetime]
            [scicloj.tableplot.v1.plotly :as plotly]
            [tablecloth.column.api :as tcc]
            [fastmath.ml.regression :as fmreg]))

;; ## Read

(tc/dataset "data/ks-projects-201801.csv.gz"
            {:num-rows 100})

(tc/dataset "data/ks-projects-201801.csv.gz"
            {:num-rows 100
             :key-fn (fn [s]
                       (-> s
                           (str/replace #" " "_")
                           keyword))})

(def raw-projects
  (tc/dataset "data/ks-projects-201801.csv.gz"
              {:key-fn (fn [s]
                         (-> s
                             (str/replace #" " "_")
                             keyword))}))

(tc/info raw-projects)


;; ## Preprocess

(def processed-projects
  (-> raw-projects
      (tc/map-columns :log10goal
                      [:usd_goal_real]
                      math/log10)
      (tc/map-columns :launched
                      [:launched]
                      (partial java-time/local-date-time
                               "yyyy-MM-dd HH:mm:ss"))
      (tc/add-column :launch-year
                     (fn [ds]
                       (datetime/long-temporal-field
                        :years
                        (:launched ds))))))

(-> processed-projects
    (tc/group-by [:launch-year :state])
    (tc/aggregate {:count tc/row-count})
    (tc/order-by [:launch-year :state]))

(def clean-projects
  (-> processed-projects
      (tc/select-rows (fn [{:keys [launch-year state]}]
                        (and (<= 2009 launch-year 2017)
                             (not= state "live"))))))

(-> clean-projects
    (tc/group-by [:launch-year :state])
    (tc/aggregate {:count tc/row-count})
    (tc/order-by [:launch-year :state]))


;; ## Explore


(-> clean-projects
    (tc/group-by [:main_category])
    (tc/aggregate {:count tc/row-count})
    (tc/order-by [:n] :desc))


(-> clean-projects
    (tc/map-columns :successful
                    [:state]
                    #(if (= % "successful")
                       1 0))
    (tc/group-by [:main_category])
    (tc/aggregate {:count tc/row-count
                   :success-rate (fn [ds]
                                   (tcc/mean (:successful ds)))})
    (tc/order-by [:success-rate] :desc))


(-> clean-projects
    (tc/map-columns :successful
                    [:state]
                    #(if (= % "successful")
                       1 0))
    (tc/group-by [:main_category])
    (tc/aggregate {:count tc/row-count
                   :success-rate (fn [ds]
                                   (tcc/mean (:successful ds)))})
    (tc/order-by [:success-rate] :desc)
    (plotly/layer-bar {:=x :main_category
                       :=y :success-rate}))


;; ## Model

(def features [:theater :log10goal])

(def split-projects
  (-> clean-projects
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
      (tc/select-columns (conj features :successful))
      (tc/split :holdout {:split-names [:train :test]
                          :seed 1})
      (tc/group-by :$split-name {:result-type :as-map})
      (update-vals #(tc/drop-columns % [:$split-name :$split-id]))))

(def train-projects
  (:train split-projects))

(def test-projects
  (:test split-projects))

(def model
  (fmreg/glm (:successful train-projects)
             (-> train-projects
                 (tc/select-columns features)
                 tc/rows)
             {:family :binomial
              :tol 0.5}))


(-> test-projects
    (tc/add-column :probability
                   (fn [ds]
                     (map model
                          (-> ds
                              (tc/select-columns features)
                              tc/rows)))))

(-> test-projects
    (tc/add-column :probability
                   (fn [ds]
                     (map model
                          (-> ds
                              (tc/select-columns features)
                              tc/rows))))
    (tc/add-column :prediction
                   (fn [ds]
                     (tcc/> (:probability ds) 0.5))))


(-> test-projects
    (tc/add-column :probability
                   (fn [ds]
                     (map model
                          (-> ds
                              (tc/select-columns features)
                              tc/rows))))
    (tc/add-column :prediction
                   (fn [ds]
                     (tcc/> (:probability ds) 0.5)))
    (tc/group-by [:prediction])
    (tc/aggregate {:count tc/row-count
                   :rate-of-success (fn [ds]
                                      (-> ds
                                          :successful
                                          tcc/mean))}))


