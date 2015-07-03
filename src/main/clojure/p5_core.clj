(ns p5-core
  (:import (processing.core.PApplet)))

(gen-class
  :name p5-core.P5ReplClj
  :extends com.generalprocessingunit.processing.P5Repl
  :exposes-methods {setup parentSetup}                      ;exposes P5Repl.setup()
  :prefix "p5-")

(import processing.core.PApplet)
(import processing.core.PVector)
(import com.generalprocessingunit.processing.space.Camera)

(def cam (Camera.))

(defn p5-setup [this]
  (.size this 1920 1000 PApplet/OPENGL)
  (.parentSetup this) ;calls P5Repl.setup()
  )

;TODO: move this and others like it to another file
(defn nice-orb [this pG]
  (.blendMode pG PApplet/ADD)


  (.noFill pG)
  (.sphereDetail pG 50)

  (.stroke pG 100 50 0 100)
  (.strokeWeight pG 5)


  (.pushMatrix pG)

  (let [r (mod (/ (.millis this) 2000) PApplet/TWO_PI)]
    (.rotateY pG r)
    )
  (if (> 0.3 (rand)) (.sphere pG 0.6))


  (.popMatrix pG)

  (.stroke pG 0 50 100 100)


  (.pushMatrix pG)
  (let [r (mod (/ (.millis this) 3000) PApplet/TWO_PI)]
    (.rotateX pG r)
    )

  (if (> 0.2 (rand)) (.sphere pG 0.7))
  (.popMatrix pG)

  (.stroke pG 50 100 50 100)

  (.pushMatrix pG)
  (let [r (mod (/ (.millis this) 5000) PApplet/TWO_PI)]
    (.rotateZ pG r)
    )
  (.sphere pG 0.8)
  (.popMatrix pG)

  (.stroke pG 244 255 255 50)

  (.pushMatrix pG)
  (let [r (mod (/ (.millis this) 1100) PApplet/TWO_PI)]
    (.rotateX pG r)
    )
  (.sphere pG 0.8)
  (.popMatrix pG)
  )

(defn drawStuff [pG p5]

  (.colorMode pG PApplet/HSB)
  (.blendMode pG PApplet/ADD)

  (defn drawSphere [x y hue]

    (.noFill pG)
    (.strokeWeight pG (rand 10))
    (.stroke pG hue 255 255 60)
    (.sphereDetail pG (rand 10))

    (.pushMatrix pG)
    (.translate pG (* x 0.1) 0 (* y 0.1))
    (.sphere pG (rand 0.1))
    (.popMatrix pG)
    )

  (defn circleOfSpheres [n r]
    (let [thetas (map (fn [x] (* x (/ PApplet/TWO_PI n))) (range 0 n))
          xs (map (fn [theta] (* r (Math/sin theta))) thetas)
          zs (map (fn [theta] (* r (Math/cos theta))) thetas)
          hues (map (fn [a] (* a (/ 255 n))) (range 0 n))]
      (doall (map drawSphere xs zs hues))
      )
    )

  (defn drawCircle [i delta r]
    (.pushMatrix pG)
    (.translate pG 0 (* i delta) 0)
    (.rotateY pG (* (if (== (mod i 2) 0) 1 -1) (/ (.millis p5) 3000)))
    (circleOfSpheres 12 r)
    (.popMatrix pG))

  (defn stacky [n t]
    (doall (map drawCircle (range 0 n) (repeat t) (range 0 n)))
    )

  (stacky 10 0.15)

  )

(defn p5-drawReplView [this pG spaceNav]
  (.camera cam pG)

  (.background pG 0 0 0 90)

  (let [[t r] [(.-translation spaceNav) (.-rotation spaceNav)]]
    (let [[x y z] [(.-x t) (.-y t) (.-z t)]]
      (.translateWRTObjectCoords cam (PVector/mult t 0.01))
      (.rotate cam r )
      )
    )

  ;(.sphere pG 0.06)
  ;(stalk this pG spaceNav)

  ;(.translate pG 1 1 1)
  ;(.sphere pG 0.1)
  ;(.translate pG 1 1 1)
  ;(.sphere pG 0.1)
  (drawStuff pG this)

  )

(defn start []
  (PApplet/main "p5-core.P5ReplClj"))

;(.yaw cam -0.1)
;(start)


