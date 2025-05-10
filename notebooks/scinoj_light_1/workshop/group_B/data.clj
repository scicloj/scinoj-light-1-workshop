

(ns scinoj-light-1.workshop.group-B.data
  (:require [tablecloth.api :as tc]
            [clojure.string :as str]))


(def raw-projects
  (tc/dataset "data/ks-projects-201801.csv.gz"
              {:key-fn (fn [s]
                         (-> s
                             (str/replace #" " "_")
                             keyword))}))


(type raw-projects)

(map? raw-projects)

(keys raw-projects)

(vals raw-projects)

(second (vals raw-projects))

raw-projects

(raw-projects :category)

(:abcd {:abcd 9
        :efgh 11})

(:category raw-projects)


(tc/info raw-projects)
