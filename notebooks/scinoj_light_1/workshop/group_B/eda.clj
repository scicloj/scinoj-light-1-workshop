

;; # Exploratory Data Analysis

(ns scinoj-light-1.workshop.group-B.eda
  (:require [scinoj-light-1.workshop.group-B.data :as data]
            [tablecloth.api :as tc]
            [tablecloth.column.api :as tcc]
            [tech.v3.dataset.print :as dsprint]
            [scicloj.kindly.v4.kind :as kind]
            [tech.v3.datatype.datetime :as datetime]
            [scicloj.tableplot.v1.plotly :as plotly]))


data/processed-projects

(-> data/processed-projects
    :currency
    distinct)

(-> data/processed-projects
    :currency
    frequencies)

(:currency (tc/unique-by data/raw-projects :currency))


(-> data/processed-projects
    (tc/unique-by :currency)
    :currency)

(-> data/processed-projects
    (tc/group-by [:currency])
    (tc/aggregate {:n tc/row-count})
    (tc/order-by [:n] :desc))

(-> data/processed-projects
    (tc/group-by [:currency]))

(-> data/processed-projects
    (tc/group-by [:currency])
    (dsprint/print-policy :repl))

(-> data/processed-projects
    (tc/group-by [:currency])
    (tc/aggregate {:n (fn [ds]
                        (tc/row-count ds))}))

(-> data/processed-projects
    (tc/group-by [:currency])
    (tc/aggregate {:n tc/row-count}))


(-> data/processed-projects
    :state
    frequencies)

;; overall rate of success
(-> data/processed-projects
    :state
    (tcc/eq "successful")
    tcc/mean)

(tcc/mean [true true false])
(tcc/mean [1 1 0])


(-> data/processed-projects
    (tc/add-column :deadline-year
                   (fn [ds]
                     (datetime/long-temporal-field
                      :years
                      (:deadline ds))))
    (tc/select-columns [:deadline-year :state])
    (tc/group-by [:deadline-year])
    (tc/aggregate {:n tc/row-count
                   :rate-of-live (fn [ds]
                                   (-> ds
                                       :state
                                       (tcc/eq "live")
                                       tcc/mean))
                   :rate-of-success (fn [ds]
                                      (-> ds
                                          :state
                                          (tcc/eq "successful")
                                          tcc/mean))})
    (tc/order-by [:deadline-year]))


(-> data/processed-projects
    (tc/add-column :deadline-year
                   (fn [ds]
                     (datetime/long-temporal-field
                      :years
                      (:deadline ds))))
    (tc/select-columns [:deadline-year :state])
    (tc/group-by [:deadline-year])
    (tc/aggregate {:n tc/row-count
                   :rate-of-live (fn [ds]
                                   (-> ds
                                       :state
                                       (tcc/eq "live")
                                       tcc/mean))
                   :rate-of-success (fn [ds]
                                      (-> ds
                                          :state
                                          (tcc/eq "successful")
                                          tcc/mean))})
    (tc/order-by [:deadline-year])
    (plotly/base {:=x :deadline-year
                  :=y :rate-of-success})
    plotly/layer-line 
    (plotly/layer-point {:=mark-size 20}))







