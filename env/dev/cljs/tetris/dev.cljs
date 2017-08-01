(ns ^:figwheel-no-load tetris.dev
  (:require
    [tetris.core :as core]
    [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(core/init!)
