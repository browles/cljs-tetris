(ns tetris.game)

(def I [[1 1 1 1]
        [0 0 0 0]])

(def S [[0 2 2]
        [2 2 0]
        [0 0 0]])

(def Z [[3 3 0]
        [0 3 3]
        [0 0 0]])

(def O [[4 4]
        [4 4]])

(def T [[0 5 0]
        [5 5 5]
        [0 0 0]])

(def J [[6 0 0]
        [6 6 6]
        [0 0 0]])

(def L [[0 0 7]
        [7 7 7]
        [0 0 0]])

(defn- rotate-piece [piece]
  (apply mapv (comp vec reverse vector) piece))

(defn- random-piece []
  (rand-nth [I S Z O T J L]))

(defn- new-board [n m]
  (->> (repeat 0)
       (take m)
       repeat
       (take n)
       (mapv vec)))

(defn new-game []
  {:board (new-board 20 10)
   :piece (random-piece)
   :next-piece (random-piece)
   :score 0
   :x 0
   :y 3
   :n 20
   :m 10
   :lost false})

(defn piece-collision? [{:keys [board piece x y] :as game}]
  (some true? (for [i (range (count piece))
                    j (range (count (first piece)))
                    :let [bx (+ x i)
                          by (+ y j)
                          p (get-in piece [i j])
                          b (get-in board [bx by] 1)]]
                (and (pos? p) (pos? b)))))

(defn get-new-piece [game]
  (assoc game
    :piece (:next-piece game)
    :next-piece (random-piece)
    :x 0
    :y (if (= O (:next-piece game)) 4 3)))

(defn check-state [game]
  (if (piece-collision? game)
    (assoc game :lost true)
    game))

(defn place-piece [{:keys [board piece x y] :as game}]
  (let [temp (atom board)]
    (dorun (for [i (range (count piece))
                 j (range (count (first piece)))
                 :let [bx (+ x i)
                       by (+ y j)
                       p (get-in piece [i j])
                       b (get-in board [bx by] 1)]
                 :when (zero? b)]
             (swap! temp #(assoc-in % [bx by] p))))
    (assoc game :board @temp)))

(defn clear-rows [{:keys [board piece x y n m] :as game}]
  (let [groups (group-by #(every? pos? %) board)
        n-cleared (count (get groups true))
        blank-row (vec (take m (repeat 0)))
        new-b (vec (concat (take n-cleared (repeat blank-row))
                           (get groups false)))]
    (-> game
        (assoc :board new-b)
        (update :score #(+ (* 10 n-cleared n-cleared) %)))))

(defn process-piece [game]
  (-> game
      place-piece
      clear-rows
      get-new-piece
      check-state))

(defn left [game]
  (update game :y dec))

(defn right [game]
  (update game :y inc))

(defn rotate [game]
  (update game :piece rotate-piece))

(defn down [game]
  (update game :x inc))

(defn try-action [f game]
  (if (:lost game)
    game
    (let [next (f game)]
      (if (piece-collision? next)
        game
        next))))

(def try-left (partial try-action left))
(def try-right (partial try-action right))
(def try-rotate (partial try-action rotate))
(def try-down (partial try-action down))

(defn gravity [game]
  (if (:lost game)
    game
    (let [next (down game)]
      (if (piece-collision? next)
        (process-piece game)
        next))))

(defn drop-piece [game]
  (->> (iterate down game)
       (take-while (comp not piece-collision?))
       last
       process-piece))
