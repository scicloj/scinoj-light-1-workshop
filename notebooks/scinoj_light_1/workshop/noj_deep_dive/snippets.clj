(ns scinoj-light-1.workshop.noj-deep-dive.snippets
  (:require [tablecloth.api :as tc]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]
            [tablecloth.column.api :as tcc]))



;; ## tech.ml.dataset / Tablecloth

;; The usual joy of Clojure is about processing JSON-like data.
;; The usual tidyverse mindset offers easy and functional table processing.

;; Here, we consoldate the two: processing rows as plain maps
;; through an efficient view of a columnwise memory representation:

(-> (rdatasets/datasets-iris)
    (tc/map-columns :petal-geometric-mean
                    [:petal-length :petal-width]
                    ;; geometric mean
                    (comp tcc/sqrt tcc/*))
    (tc/select-rows (fn [{:keys [petal-geometric-mean]}]
                      (< petal-geometric-mean 2)))
    (tc/group-by [:species])
    (tc/aggregate {:n tc/row-count}))

