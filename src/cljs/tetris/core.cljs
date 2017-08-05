(ns tetris.core
  (:require [reagent.core :as reagent]
            [tetris.game :as game]))

(enable-console-print!)

(defonce game-state (reagent/atom (game/new-game)))

(defn get-action [key]
  (case key
    ("k" "ArrowUp") game/try-rotate
    ("l" "ArrowRight") game/try-right
    ("h" "ArrowLeft") game/try-left
    ("j" "ArrowDown") game/gravity
    " " game/drop-piece))

(defn key-handler [e]
  (let [key (.-key e)
        action (get-action key)]
    (when action
      (.preventDefault e)
      (swap! game-state action))))

(defn tick []
  (let [wait (-> (:score @game-state)
                 (/ 5000)
                 inc
                 js/Math.log
                 inc
                 (->> (/ 1000)))]
    (swap! game-state game/gravity)
    (js/setTimeout tick wait)))

(defn init []
  (do (js/setTimeout tick 1000)
      (.addEventListener js/window "keydown" key-handler)))

(defonce start (init))

(def enum->color
  {0 "empty"
   1 "blue"
   2 "red"
   3 "yellow"
   4 "purple"
   5 "orange"
   6 "cyan"
   7 "brown"})

(defn to-transform [x y w]
  (str "translate(" (inc (* y w)) "px," (inc (* x w)) "px)"))

(defn grid-view
  ([element rows]
   (grid-view element nil rows))
  ([element props rows]
   [element props
    (map-indexed
      (fn [i row]
        ^{:key i}
        [:div.row
         (map-indexed
           (fn [j item]
             ^{:key (str i "-" j)}
             [:div.cell
              {:className (enum->color item)}])
           row)])
      rows)]))

(def board-view (partial grid-view :div.board))
(def piece-view (partial grid-view :div.piece))
(def next-piece-view (partial grid-view :div.next.piece))
(def ghost-view (partial grid-view :div.ghost))

(defn score-view [score]
  [:div.score score])

(defn game-view []
  (let [{:keys [x y]} @game-state
        ghost (->> (iterate game/down @game-state)
                   (take-while (comp not game/piece-collision?))
                   last)
        gx (:x ghost)
        gy (:y ghost)]
    [:div.tetris-container
     [board-view (:board @game-state)]
     [score-view (:score @game-state)]
     [piece-view {:style {:transform (to-transform x y 50)}} (:piece @game-state)]
     [next-piece-view {:style {:transform (to-transform 0 0 50)}} (:next-piece @game-state)]
     [ghost-view {:style {:transform (to-transform gx gy 50)}} (:piece @game-state)]]))

(defn mount-root []
  (reagent/render [game-view] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
