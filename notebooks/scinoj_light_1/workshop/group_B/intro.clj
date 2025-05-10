(ns scinoj-light-1.workshop.group-B.intro)

;; https://www.kaggle.com/datasets/kemical/kickstarter-projects

(def data 
  [{:ID 244511278,
    :name "Tomárâho Dictionary",
    :category "Publishing",
    :main_category "Publishing",
    :currency "USD",
    :deadline "2013-06-20",
    :goal 7000.0,
    :launched "2013-05-21 01:31:04",
    :pledged 2675.0,
    :state "failed",
    :backers 37,
    :country "US",
    :usd_pledged 2675.0,
    :usd_pledged_real 2675.0,
    :usd_goal_real 7000.0}
   {:ID 2140575984,
    :name "Porcelain doll with handmade crochet dress",
    :category "Crochet",
    :main_category "Crafts",
    :currency "USD",
    :deadline "2015-03-27", 
    :goal 500.0, 
    :launched "2015-02-25 23:11:03", 
    :pledged 0.0, 
    :state "failed",
    :backers 0, 
    :country "US", 
    :usd_pledged 0.0, 
    :usd_pledged_real 0.0,
    :usd_goal_real 500.0}])


data

(type data)

(vector? data)

(count data)

(nth data 0)

(data 0)

(fn? data)

(def data0
  (data 0))

data0

(type data0)

(keys data0)

(vals data0)


(vals (data 0))

(-> 0
    data
    vals)

(macroexpand-1
 '(-> 0
    data
    vals))
