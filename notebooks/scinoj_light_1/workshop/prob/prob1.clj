;; # Probability and statistics

;; ## Setup

(ns scinoj-light-1.workshop.prob.prob1
  (:require [scinoj-light-1.workshop.prob.data :as data]
            [fastmath.random :as random]
            [tablecloth.api :as tc]
            [scicloj.tableplot.v1.plotly :as plotly]
            [tablecloth.column.api :as tcc]
            [clojure.math :as math]
            [fastmath.ml.regression :as fmreg]
            [fastmath.vector :as fmvec]
            [fastmath.stats :as stats]
            [inferme.core :as inferme]
            [clojure.string :as str]
            [java-time.api :as java-time]
            [tech.v3.datatype.datetime :as datetime]))



;; probability

;; statistics

;; descriptive statistics

;; statistical inference

;; Bayesian statistics


;; Fastmath 3 WIP docs
;; https://generateme.github.io/fastmath/clay/

;; Inferme (probabilistic programming)
;; https://github.com/generateme/inferme


(let [rng (random/rng :jdk 1)]
  (->> #(random/irandom rng 6)
       (repeatedly 100)
       (reduce +)))

(defn seed->game [seed]
  (let [rng (random/rng :jdk seed)]
    (->> #(random/irandom rng 6)
         (repeatedly 100)
         (reduce +))))

(->> (range 10)
     (map seed->game))

(defn rng->game [rng]
  (->> #(random/irandom rng 6)
       (repeatedly 100)
       (reduce +)))

(let [rng (random/rng :jdk 1)]
  (repeatedly 10 #(rng->game rng)))



(let [rng (random/rng :jdk 1)]
  (-> {:x (->> #(rng->game rng)
               (repeatedly 10000))}
      tc/dataset
      (plotly/layer-histogram {:=histogram-nbins 50})))


(def game-data
  (let [rng (random/rng :jdk 1)]
    (-> {:x (->> #(rng->game rng)
                 (repeatedly 10000))}
        tc/dataset)))


(-> game-data
    (plotly/layer-histogram {:=histogram-nbins 50}))

(-> game-data
    tc/info)


(-> game-data
    :x
    stats/mean)

(-> game-data
    :x
    stats/stddev)

(-> {:z (-> game-data
            :x
            stats/standardize)}
    tc/dataset
    (plotly/layer-histogram {:=x :z
                             :=histogram-nbins 50}))

(let [d (random/distribution :normal)]
  (-> {:z (range -4 4 0.01)}
      tc/dataset
      (tc/map-columns :density
                      [:z]
                      #(random/pdf d %))
      (plotly/layer-line {:=x :z
                          :=y :density})))



;; ## Probabilistic programming


(-> {:x (-> (inferme/infer :metropolis-hastings
                           (inferme/make-model
                            []
                            (let [d (inferme/distr :normal
                                                   {:mu 10})
                                  x (inferme/sample d)]
                              (inferme/model-result []
                                                    {:x x})))
                           {:samples 10000})
            (inferme/trace :x))}
    tc/dataset
    (plotly/layer-histogram {:=histogram-nbins 100}))


(-> {:x (-> (inferme/infer :metropolis-hastings
                           (inferme/make-model
                            []
                            (let [d (inferme/distr :normal
                                                   {:mu 10})
                                  x (inferme/sample d)]
                              (inferme/model-result []
                                                    {:x x})))
                           {:samples 10000})
            (inferme/trace :x))}
    tc/dataset
    (tc/select-rows (fn [{:keys [x]}]
                      (< x 11)))
    (plotly/layer-histogram {:=histogram-nbins 100}))





(-> {:x (-> (inferme/infer :metropolis-hastings
                           (inferme/make-model
                            []
                            (let [d (inferme/distr :normal
                                                   {:mu 10})
                                  x (inferme/sample d)]
                              (inferme/model-result [(inferme/condition
                                                      (< x 11))]
                                                    {:x x})))
                           {:samples 10000})
            (inferme/trace :x))}
    tc/dataset
    (plotly/layer-histogram {:=histogram-nbins 100}))





(-> {:x (-> (inferme/infer :metropolis-hastings
                           (inferme/make-model
                            [m (inferme/distr :exponential)]
                            (let [d (inferme/distr :normal
                                                   {:mu m})
                                  x (inferme/sample d)]
                              (inferme/model-result [(inferme/observe
                                                      d
                                                      [10 11 10 11 10 11])]
                                                    {:x x})))
                           {:samples 10000})
            (inferme/trace :x))}
    tc/dataset
    (plotly/layer-histogram {:=histogram-nbins 100}))



(-> {:m (-> (inferme/infer :metropolis-hastings
                           (inferme/make-model
                            [m (inferme/distr :exponential)]
                            (let [d (inferme/distr :normal
                                                   {:mu m})
                                  x (inferme/sample d)]
                              (inferme/model-result [(inferme/observe
                                                      d
                                                      [10 11 10 11 10 11])]
                                                    {:x x})))
                           {:samples 10000})
            (inferme/trace :m))}
    tc/dataset
    (tc/add-column :i (range))
    (plotly/layer-line {:=x :i
                        :=y :m
                        :=histogram-nbins 100}))


;; ## Read & preprocess

(def clean-projects
  (-> (tc/dataset "data/ks-projects-201801.csv.gz"
                  {:key-fn (fn [s]
                             (-> s
                                 (str/replace #" " "_")
                                 keyword))})
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
                        (:launched ds))))
      (tc/select-rows (fn [{:keys [launch-year state]}]
                        (and (<= 2009 launch-year 2017)
                             (not= state "live"))))
      (tc/map-columns :theater
                      [:main_category]
                      #(if (= % "Theater")
                         1 0))
      (tc/map-columns :log10goal
                      [:usd_goal_real]
                      math/log10)
      (tc/map-columns :successful
                      [:state]
                      #(if (= % "successful")
                         0 1))
      (tc/map-columns :namelen
                      [:name]
                      count)))


;; ## logistic regression

(def features [:theater :log10goal])


(let [coefficients [-2 -1 2/3]
      probabilities  (->> features
                          (map-indexed (fn [i feature]
                                         (tcc/* (projects feature)
                                                (coefficients (inc i)))))
                          (reduce tcc/+ (coefficients 0))
                          double-array
                          fmvec/sigmoid)
      rng (random/rng :isaac 1)]
  (map <
       (repeatedly #(random/frandom rng))
       probabilities))


(def model
  (fmreg/glm (:successful projects)
             (-> projects
                 (tc/select-columns features)
                 tc/rows)
             {:family :binomial
              :tol 0.5
              :names [:theater :log10goal]}))


(->> model
     :coefficients
     (map :estimate)
     (zipmap (cons :intercept features)))




(let [projects-sample (-> projects
                          (tc/random 1000 {:seed 1}))]
  (inferme/defmodel model-1
    [coefficients (inferme/multi :normal
                                 3)]
    (let [probabilities  (->> features
                              (map-indexed (fn [i feature]
                                             (tcc/* (projects-sample feature)
                                                    (coefficients
                                                     (inc i)))))
                              (reduce tcc/+ (coefficients 0))
                              double-array
                              fmvec/sigmoid)
          binomials (->> probabilities
                         (map (fn [p]
                                (inferme/distr :binomial {:trials 1 :p p}))))]
      (inferme/model-result (map random/lpdf
                                 binomials
                                 (:successful projects-sample))))))

(def res (time
          (inferme/infer :metropolis-hastings
                         model-1
                         {:samples 10000
                          :max-time 60})))

(-> res
    (inferme/trace :coefficients)
    (tc/dataset {:column-names (cons :intercept features)})
    (tc/add-column :i (range))
    (tc/pivot->longer (cons :intercept features))
    (plotly/layer-line {:=x :i
                        :=y :$value
                        :=color :$column}))

(-> res
    (inferme/trace :coefficients)
    (tc/dataset {:column-names (cons :intercept features)})
    tc/pivot->longer
    (plotly/layer-histogram {:=x :$value
                             :=color :$column
                             :=mark-opacity 0.5
                             :=histogram-nbins 100}))

(let [projects-sample (-> projects
                          (tc/random 1000 {:seed 1}))
      model (inferme/make-model
             [coeff0 (inferme/distr :normal)
              coeff1 (inferme/distr :normal)
              coeff2 (inferme/distr :normal)]
             (let [probabilities (-> (tcc/+ coeff0
                                            (tcc/* coeff1 (:theater projects-sample))
                                            ;; (tcc/* coeff2 (:log10goal projects-sample))
                                            )
                                     double-array
                                     fmvec/sigmoid)
                   binomials (->> probabilities
                                  (map (fn [p]
                                         (inferme/distr :binomial {:trials 1 :p p}))))]
               (inferme/model-result (map random/lpdf
                                          binomials
                                          (:succeessful projects-sample)))))
      samples (inferme/infer :metropolis-hastings
                             model
                             {:samples 10000
                              :max-time 60})]
  (-> samples
      :accepted
      tc/dataset
      (tc/select-columns [:coeff0 :coeff1 :coeff2])
      (tc/add-column :i (range))
      (tc/pivot->longer [:coeff0 :coeff1 :coeff2])
      (plotly/layer-line {:=x :i
                          :=y :$value
                          :=color :$column})))





















(inferme/defmodel logistic-model-1
  [coefficients (inferme/multi :normal
                               (inc (count features))
                               {:mu 0 :sd 1000})]
  (let [probabilities  (->> features
                            (map-indexed (fn [i feature]
                                           (tcc/* (projects feature)
                                                  (coefficients
                                                   (inc i)))))
                            (reduce tcc/+ (coefficients 0))
                            double-array
                            fmvec/sigmoid)
        _ (prn probabilities)
        binomials (->> probabilities
                       (map (fn [p]
                              (inferme/distr :binomial {:trials 1 :p p}))))]
    (inferme/model-result (map (fn [b s]
                                 (inferme/observe1 b s))
                               binomials
                               (:successful projects))
                          ;; {:intercept intercept}
                          )))


(def res (time
          (inferme/infer :metropolis-hastings
                         logistic-model-1
                         {:initial-point [[0 0 0]]})))

(-> res
    (inferme/trace :intercept))



