

;; # Exploratory Data Analysis
(ns scinoj-light-1.workshop.group-A.eda
  (:require [tablecloth.api :as tc]
            [tablecloth.column.api :as tcc]
            [scinoj-light-1.workshop.group-A.data :as data]
            [tech.v3.dataset.print :as dsprint]
            [tech.v3.datatype.datetime :as datetime]
            [scicloj.tableplot.v1.plotly :as plotly])) 


data/processed-projects


(-> data/processed-projects
    (tc/group-by [:category])
    (tc/aggregate {:n tc/row-count})
    (tc/order-by [:n] :desc))


(-> data/processed-projects
    (tc/group-by [:category])
    (dsprint/print-policy :repl))



(-> data/processed-projects
    (tc/group-by [:category])
    (dsprint/print-policy :repl)
    (tc/aggregate {:n tc/row-count 
                   #_(fn [ds] (tc/row-count ds))})
    (tc/order-by [:n] :desc))


(tcc/eq ["ABCD" "EFGH" "ABCD" "ABCD" "IJKL"]
        "ABCD")

(tcc/mean [true false true true false])

(tcc/mean
 (tcc/eq ["ABCD" "EFGH" "ABCD" "ABCD" "IJKL"]
         "ABCD"))

(-> data/processed-projects
    :state
    (tcc/eq "successful")
    tcc/mean)

(-> data/processed-projects
    (tc/group-by [:category])
    (tc/aggregate {:n tc/row-count
                   :rate-of-success (fn [ds]
                                      (-> ds
                                          :state
                                          (tcc/eq "successful")
                                          tcc/mean))})
    (tc/order-by [:rate-of-success] :desc))


(-> data/processed-projects
    :category
    seq)


(-> data/processed-projects
    :category
    second)

(-> data/processed-projects
    :category
    frequencies)

(-> data/processed-projects
    :category
    (nth 1000))

(-> data/processed-projects
    :goal)

[4 2 4]

[3 :a "s"]

;; java boxing
;; a primitive float value
;; a boxed Float value

(float-array [4 2 4])

(-> data/processed-projects
    :goal
    .data)

data/processed-projects


(-> data/processed-projects
    (tc/group-by [:category :country])
    (tc/aggregate {:n tc/row-count
                   :rate-of-success (fn [ds]
                                      (-> ds
                                          :state
                                          (tcc/eq "successful")
                                          tcc/mean))})
    (tc/order-by [:n] :desc))


(-> data/processed-projects
    tc/rows
    (->> (take 9)))

(-> data/processed-projects
    tc/rows
    first
    vector?)


(-> data/processed-projects
    tc/rows
    first
    type)

(-> data/processed-projects
    (tc/rows :as-maps)
    (->> (take 9)))


(-> data/processed-projects
    (tc/rows :as-maps)
    first
    map?)

(-> data/processed-projects
    (tc/rows :as-maps)
    first
    type)




(-> data/processed-projects
        (tc/group-by [:category :country])
        (tc/aggregate {:n tc/row-count
                       :rate-of-success (fn [ds]
                                          (-> ds
                                              :state
                                              (tcc/eq "successful")
                                              tcc/mean))})
        (tc/order-by [:n] :desc)
        (tc/select-rows (fn [row]
                          (#{"Product Design" "Music"} (:category row)))))

#_(datetime/long-temporal-field
 :years
 (data/processed-projects :launched))



(-> data/clean-projects
    (tc/group-by [:launch-year])
    (tc/aggregate {:n tc/row-count
                   :rate-of-success (fn [ds]
                                      (-> ds
                                          :state
                                          (tcc/eq "successful")
                                          tcc/mean))})
    (tc/order-by [:launch-year]))



(-> data/clean-projects
    (tc/group-by [:launch-year])
    (tc/aggregate {:n tc/row-count
                   :rate-of-success (fn [ds]
                                      (-> ds
                                          :state
                                          (tcc/eq "successful")
                                          tcc/mean))})
    (tc/order-by [:launch-year])
    (plotly/layer-line {:=x :launch-year
                        :=y :rate-of-success}))

