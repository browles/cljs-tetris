(ns tetris.prod
  (:require [tetris.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)