;; # Intro to Noj

;; https://www.kaggle.com/datasets/kemical/kickstarter-projects

;; https://scicloj.github.io/noj/


;; cynosure 
;; scinojure

;; ## Setup

(ns scinoj-light-1.workshop.start.walkthrough
  (:require [tablecloth.api :as tc] ; Tablecloth is inspired by dplyr and friends.
            [clojure.string :as str]
            [clojure.math :as math]
            [java-time.api :as java-time]
            [tech.v3.datatype.datetime :as datetime]
            [tech.v3.dataset.print :as dsprint]
            [tech.v3.datatype.datetime :as datetime]
            [scicloj.tableplot.v1.plotly :as plotly] ;; Tableplot is inspired by ggplot
            ;; .. but also has a Plotly.js target (like ggplot does).
            [tablecloth.column.api :as tcc]
            [fastmath.ml.regression :as fmreg]))

;; ## Read

(tc/dataset "data/ks-projects-201801.csv.gz"
            {:num-rows 100})

(-> (tc/dataset "data/ks-projects-201801.csv.gz"
                {:num-rows 100})
    map?)

(-> (tc/dataset "data/ks-projects-201801.csv.gz"
                {:num-rows 100})
    keys)



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

raw-projects

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
    tc/info)




(-> processed-projects
    (tc/group-by [:launch-year :state])
    (tc/aggregate {:count tc/row-count})
    (tc/order-by [:launch-year :state]))



(def clean-projects
  (-> processed-projects
      (tc/select-rows (fn [{:keys [launch-year state]}]
                        (and (<= 2009 launch-year 2017)
                             (not= state "live"))))))



(-> {:x (range 6)
     :y [:A :B :A :B :A :B]}
    tc/dataset
    (tc/map-columns :z
                    [:x :y]
                    (fn [x y]
                      (str x "~~~~" y))))


(-> {:x (range 6)
     :y [:A :B :A :B :A :B]}
    tc/dataset
    (tc/add-column :z
                   (fn [ds]
                     (map #(* 1000 %)
                          (:x ds)))))

(-> {:x (range 6)
     :y [:A :B :A :B :A :B]}
    tc/dataset
    (tc/add-column :z
                   (fn [ds]
                     (tcc/* (:x ds) 1000))))

(-> clean-projects
    (tc/group-by [:launch-year :state]))

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

(keys split-projects)

(def train-projects
  (:train split-projects))

(def test-projects
  (:test split-projects))

;; general linear model
;; (specifically, we use logistic regression with the sigmoid function)

;; probaility of success = sigmoid ( A + B * theater + C * log10goal )

(def model
  (fmreg/glm (:successful train-projects)
             (-> train-projects
                 (tc/select-columns features)
                 tc/rows)
             {:family :binomial ; which means estimating probabilities
              :tol 0.5 ; for numerical tolerance
              }))

(model [0 4])
(model [1 4])

(-> model
    :coefficients)


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


