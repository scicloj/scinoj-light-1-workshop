(ns scinoj-light-1.workshop.group-A.data
  (:require [tablecloth.api :as tc]
            [clojure.string :as str]))

(def raw-projects
  (tc/dataset "data/ks-projects-201801.csv.gz"
              {:key-fn (fn [s]
                         (-> s
                             (str/replace #" " "_")
                             keyword))}))

raw-projects

(type raw-projects)

(map? raw-projects)

(keys raw-projects)

(def category-column
  (raw-projects :category))

(type category-column)

(second category-column)

;; take only the name and category and state columns
;; and take a sample of rows

(-> raw-projects
    (tc/select-columns 
     [:name :category :state] )
    (tc/random 5))

(keyword "category")
