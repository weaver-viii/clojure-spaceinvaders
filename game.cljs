(ns game)

(def keyStates (atom {}))

(defn context [width height]
  (let [target (.getElementById js/document "target")]
    [
      (.getContext target "2d") 
      (set! (. target -width) width)
      (set! (. target -height) height)
    ]
  )
)

(defn clearScreen [[ctx width height]]
  (set! (. ctx -fillStyle) "#FFF")
  (.clearRect ctx 0 0 width height) 
)

(defn drawSquare [[ctx width height] x y w h c]
  (set! (. ctx -fillStyle) c)
  (.fillRect ctx x y w h) 
)

(defn initEnemy [x y w h]
 {
  :x (* x 30)
  :y (* y 30)
  :w w
  :h h
 }
)

(defn initPlayer [x y w h]
 {
  :x x
  :y y
  :w w
  :h h
 }
)

(defn initBullet [x y w h]
 {
  :x x
  :y y
  :w w
  :h h
 }
)

(defn initState []
 { 
   :direction 1
   :enemies (for [x (range 0 16 2)
                  y (range 0 8 2)]
              (initEnemy x y 20 20)
   )
   :player (initPlayer 200 430 20 20)
   :bullets '()
 } 
)

(defn directionLogic [state]
  (let [{:keys [direction enemies]} state]
    (if (= direction 1)
      (let [right (apply max (map :x enemies))]
        (if(> right 600) -1 1)
      )
      (let [left (apply min (map :x enemies))]
        (if(< left 0) 1 -1)
      )
    )
  )
)

(defn enemiesLogic [state]
  (let [{:keys [direction enemies]} state
        func (if(= direction 1) inc dec)
       ]
    (for [enemy enemies]
      {
        :x (func (:x enemy))
        :y (:y enemy)
        :w (:w enemy)
        :h (:h enemy)
      }
    )
  )
)

(defn bulletsLogic [state]
  (for [bullet (:bullets state)]
    {
      :x (:x bullet)
      :y (dec (:y bullet))
      :w (:w bullet)
      :h (:h bullet)
    }
  )
)

(defn applyMod [m k func]
  (assoc m k (func (m k)))
)

(defn playerLogic [state]
  (let [player (:player state)  
        left (@keyStates 37)
        right (@keyStates 39)
       ]
    (cond (= left true) (applyMod player :x dec)
          (= right true) (applyMod player :x inc)
          :else player
    )
  )
)


(defn enemiesRender [ctx state]
  (let [enemies (:enemies state)]
    (doseq [enemy enemies] 
      (let [{:keys [x y w h]} enemy]
        (drawSquare ctx x y w h "#FF0")
      )
    )
  )
)

(defn bulletsRender [ctx state]
  (doseq [bullet (:bullets state)] 
    (let [{:keys [x y w h]} bullet]
      (drawSquare ctx x y w h "#000")
    )
  )
)

(defn playerRender [ctx state]
  (let [player (:player state)]
    (let [{:keys [x y w h]} player]
      (drawSquare ctx x y w h "#F00")
    )
  )
)

(defn doLogic [state]
  {
    :direction (directionLogic state)
    :enemies (enemiesLogic state)
    :player (playerLogic state)
    :bullets (bulletsLogic state)
  }
)

(defn renderScene [ctx state]
  (enemiesRender ctx state)
  (playerRender ctx state)
)

(defn tick [ctx state]
  (clearScreen ctx) 
  (renderScene ctx state)
  (js/setTimeout (fn []
    (tick ctx (doLogic state))
  ) 33  )
)

(defn ^:export init []
  (hookInputEvents)
  (let [ctx (context 640 480)] 
    (tick ctx (initState)) 
  )
)

(defn hookInputEvents []
  (.addEventListener js/document "keydown" 
   (fn [e]
    (setKeyState (. e -keyCode) true)
     false
   )
  )
  (.addEventListener js/document "keyup" 
   (fn [e]
    (setKeyState (. e -keyCode) false)
     false
   )
  )
)

(defn setKeyState [code, value]
  (swap! keyStates assoc code value)
)
