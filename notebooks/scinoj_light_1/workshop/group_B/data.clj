

(ns scinoj-light-1.workshop.group-B.data
  (:require [tablecloth.api :as tc]
            [clojure.string :as str]
            [java-time.api :as java-time]))


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


(- (+ (* 1 1000) 200) 30)

(-> 1
    (* 1000)
    (+ 200)
    (- 30))

(macroexpand-1
 '(-> 1
    (* 1000)
    (+ 200)
    (- 30)))

(- (+ (* 1 1000) 200) 30)


raw-projects


(tc/random 
 (tc/select-columns raw-projects
                    [:name :category :usd_goal_real])
 5
 {:seed 1})


(-> raw-projects
    (tc/select-columns [:name :category :usd_goal_real])
    (tc/random 5 {:seed 1}))


(count "ABCDEF")

(-> raw-projects
    (tc/map-columns :name-length
                    [:name]
                    count)
    (tc/select-columns [:name :name-length]))


(java-time/local-date-time "yyyy-MM-dd HH:mm:ss" 
                           "2015-08-11 12:12:28")

(def processed-projects
  (-> raw-projects
      (tc/map-columns :launched
                      [:launched]
                      (partial java-time/local-date-time
                               "yyyy-MM-dd HH:mm:ss"))))

