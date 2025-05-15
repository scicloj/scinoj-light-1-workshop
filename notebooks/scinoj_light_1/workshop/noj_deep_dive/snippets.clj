(ns scinoj-light-1.workshop.noj-deep-dive.snippets
  (:require [tablecloth.api :as tc]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]
            [tablecloth.column.api :as tcc]))















;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; hamf example 1 --- faster drop-in replacements for many operations
(time (->> (range 10000000)
           (shuffle)
           (partition-all 10000)
           (map sort)
           (count)))

(require '[ham-fisted.api :as hamf]
         '[ham-fisted.lazy-noncaching :as lznc])

(time (->> (hamf/range 10000000)
           (hamf/shuffle)
           (lznc/partition-all 10000)
           (lznc/map hamf/sort)
           (count)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; hamf example 2 --- sets with faster operations (that still respect clojure equality)

(def s (java.util.HashSet.))
(.add s (long 1))
(.add s (int 1))
s

;; clojure knows:
(def cs #{})
(conj cs (long 1) (int 1))

;; hamf equivalent:
(def hs (hamf/mut-set))
(hamf/conj! hs (long 1))
(hamf/conj! hs (int 1))

;; but it's faster:
(time (def _ (set (range 1000000))))
(time (def _ (hamf/mut-set (range 1000000))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; hamf example 3 --- similar with maps
(def m (java.util.HashMap.))
(.put m (long 1) "uh")
(.put m (int 1) "oh")
m

(def hm (hamf/mut-map))
(hamf/assoc! hm (long 1) "uh")
(hamf/assoc! hm (int 1) "oh")
hm

;;;;;;;;;;;;;;;;;;;;
;; dtype - png bytes
(require '[tech.v3.libs.buffered-image :as bufimg])

(bufimg/load "https://raw.githubusercontent.com/cnuernber/dtype-next/master/test/data/test.jpg")

(def i *1)
(require '[tech.v3.datatype :as dt])

(dt/shape i)

(require '[tech.v3.tensor :as dtt])
(def t (dtt/ensure-tensor i))

;; top left pixel
(t 0 0)

;; one row down
(t 1 0)

;; Note, uint8 is also a nice type for representing this data, typically much less convenient to work with on the JVM

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; dtype - dfn/+ between ds columns
(require '[tech.v3.dataset :as ds])

(def ds (ds/->>dataset {:a [1 2 3]
                        :b [39 40 41]}))
ds

(require '[tech.v3.datatype.functional :as dfn])

(dfn/+ (:a ds) (:b ds))

;; ## tech.ml.dataset / Tablecloth

;; The usual joy of Clojure is about processing JSON-like data.
;; The usual tidyverse mindset offers easy and functional table processing.

;; Here, we consoldate the two: processing rows as plain maps
;; through an efficient view of a columnwise memory representation:

(require '[tablecloth.api :as tc]
         '[scicloj.metamorph.ml.rdatasets :as rdatasets]
         '[tablecloth.column.api :as tcc])

(-> (rdatasets/datasets-iris)
    (tc/map-columns :petal-geometric-mean
                    [:petal-length :petal-width]
                    ;; geometric mean
                    (comp tcc/sqrt tcc/*))
    (tc/select-rows (fn [{:keys [petal-geometric-mean]}]
                      (< petal-geometric-mean 2)))
    (tc/group-by [:species])
    (tc/aggregate {:n tc/row-count}))


