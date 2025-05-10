

;; # Exploratory Data Analysis
(ns scinoj-light-1.workshop.group-A.eda
  (:require [tablecloth.api :as tc]
            [tablecloth.column.api :as tcc]
            [scinoj-light-1.workshop.group-A.data :as data]
            [tech.v3.dataset.print :as dsprint])) 


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
