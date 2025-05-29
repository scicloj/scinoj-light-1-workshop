(ns scinoj-light-1.workshop.noj-deep-dive.snippets
  (:require [tablecloth.api :as tc]
            [scicloj.metamorph.ml.rdatasets :as rdatasets]
            [tablecloth.column.api :as tcc]
            [scicloj.tableplot.v1.plotly :as plotly]
            [fastmath.random :as random]
            [tech.v3.datatype :as dt]))





;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## A little example



(let [n 100]
  (-> {:x (range n)}
      tc/dataset
      (tc/add-column :y
                     (fn [{:keys [x]}]
                       (tcc/+ 9
                              (->> (dt/make-reader
                                    :float32 n (random/grand))
                                   (reductions +))
                              (tcc/* 0.1 x)
                              (tcc/sq (tcc/* 0.1 (tcc/- x 50))))))
      tc/dataset
      plotly/layer-line
      (plotly/layer-smooth {:=design-matrix [[:x '(identity :x)]
                                             [:x2 '(* :x :x)]]})))





(def ds
  (-> {:x (repeatedly 9 rand)}
      tc/dataset
      (tc/add-column :i (range))))

(type ds)

(map? ds)

(keys ds)

(vals ds)

(-> ds
    (tc/rows :as-maps))

(-> ds
    (tc/rows :as-maps)
    first
    map?)

(-> ds
    (tc/rows :as-maps)
    first
    type)

(-> ds
    (tc/rows :as-maps)
    first
    (assoc :z 99))

(-> ds
    (tc/rows :as-maps)
    first
    (assoc :z 99)
    type)

(-> ds
    tc/rows)

(-> ds
    tc/rows
    first
    vector?)

(-> ds
    tc/rows
    first
    type)

(-> ds
    tc/rows
    first
    (conj 99))

(-> ds
    tc/rows
    first
    (conj 99)
    type)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## hamf example 1 --- faster drop-in replacements for many operations
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

"lazy and noncaching"


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## hamf example 2 --- sets with faster operations (that still respect clojure equality)

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## hamf example 3 --- similar with maps
(def m (java.util.HashMap.))
(.put m (long 1) "uh")
(.put m (int 1) "oh")
m

(def hm (hamf/mut-map))
(hamf/assoc! hm (long 1) "uh")
(hamf/assoc! hm (int 1) "oh")
hm

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## dtype - png bytes
(require '[tech.v3.libs.buffered-image :as bufimg])

(def i
  (bufimg/load "https://raw.githubusercontent.com/cnuernber/dtype-next/master/test/data/test.jpg"))

i

(type i)

(require '[tech.v3.datatype :as dt])

(dt/shape i)

(require '[tech.v3.tensor :as dtt])
(def t (dtt/ensure-tensor i))

t


;; top left pixel
(t 0 0)

;; one row down
(t 1 0)

;; Note, uint8 is also a nice type for representing this data, typically much less convenient to work with on the JVM




((dfn/* t 1000)
 4 5)


(type (dfn/* t 1000))











;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## dtype - dfn/+ between ds columns
(require '[tech.v3.dataset :as ds])

(def ds (ds/->>dataset {:a [1 2 3]
                        :b [39 40 41]}))
ds

(require '[tech.v3.datatype.functional :as dfn])

(map? ds)
(keys ds)

(-> ds
    (assoc :c (dfn/+ (:a ds) (:b ds))))

ds


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Metamorph.ml




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Tableplot



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Future hopes

;; - compatibility

;; - ergonomics (https://scicloj.github.io/tablemath/)

;; - interop

;; - [std.lang](https://scicloj.github.io/stdlang-docs/)

