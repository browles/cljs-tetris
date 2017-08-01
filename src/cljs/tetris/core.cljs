(ns tetris.core
  (:require [accountant.core :as accountant]
            [reagent.core :as reagent]
            [secretary.core :as secretary :include-macros true]
            [tetris.game :as game]))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to tetris"]
   [:div [:a {:href "/about"} "go to about page"]]])

(defn about-page []
  [:div [:h2 "About tetris"]
   [:div [:a {:href "/"} "go to the home page"]]])

(enable-console-print!)
;; -------------------------
;; Routes

(def page (atom #'home-page))

(defonce states (atom []))
(defonce game-state (reagent/atom (game/new-game)))

(defn undo [])

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

(defn grid-view [element rows props]
  [element props
   (map-indexed
     (fn [i row]
       ^{:key i} [:div.row
                  (map-indexed
                    (fn [j item]
                      ^{:key (str i "-" j)} [:div.cell
                                             {:className (enum->color item)}])
                    row)])
     (rows @game-state))])

(def board-view (partial grid-view :div.board :board))
(def piece-view (partial grid-view :div.piece :piece))
(def ghost-view (partial grid-view :div.ghost :piece))

(defn score-view []
  [:div.score (:score @game-state)])

(def actions {"ArrowUp" game/try-rotate
              "ArrowRight" game/try-right
              "ArrowLeft" game/try-left
              "ArrowDown" game/try-down
              " " game/drop-piece})

(defn key-handler [e]
  (let [key (.-key e)
        action (get actions key)]
    (when action
      (.preventDefault e)
      (swap! states #(conj % @game-state))
      (swap! game-state action))))

(defonce start (do (js/setInterval #(swap! game-state game/gravity) 1000)
                   (.addEventListener js/window "keydown" key-handler)))

(defn game-view []
  (let [{:keys [x y]} @game-state
        ghost (->> (iterate game/down @game-state)
                   (take-while (comp not game/piece-collision?))
                   last)
        gx (:x ghost)
        gy (:y ghost)]
    [:div.tetris-container
     [board-view]
     (when-not (:lost @game-state)
       [piece-view {:style {:transform (to-transform x y 50)}}])
     (when-not (:lost @game-state)
       [ghost-view {:style {:transform (to-transform gx gy 50)}}])
     [score-view]]))

(secretary/defroute "/" []
  (reset! page #'home-page))

(secretary/defroute "/about" []
  (reset! page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [game-view] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
       (fn [path]
         (secretary/dispatch! path))
     :path-exists?
       (fn [path]
         (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
