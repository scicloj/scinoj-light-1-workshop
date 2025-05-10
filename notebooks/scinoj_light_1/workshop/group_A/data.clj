(ns scinoj-light-1.workshop.group-A.data
  (:require [tablecloth.api :as tc]
            [clojure.string :as str]
            [clojure.math :as math]
            [java-time.api :as java-time]))

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

(first 
 [4 1 4])


(tc/info raw-projects)


(:launched raw-projects)


(math/log10 1000)  ; 10*10*10 = 1000

(map math/log10
 [1 10 100 1000 10000])


(def processed-projects
  (-> raw-projects
      (tc/map-columns :log10goal
                      [:usd_goal_real]
                      math/log10)
      (tc/map-columns :back-ratio
                      [:usd_pledged_real :backers]
                      (fn [u b]
                        (if (zero? b)
                          nil
                          (/ u b))))
      (tc/map-columns :launched
                      [:launched]
                      (partial java-time/local-date-time "yyyy-MM-dd HH:mm:ss"))))


(java-time/local-date-time
 "yyyy-MM-dd HH:mm:ss"
 "2016-01-13 18:13:53")


processed-projects
