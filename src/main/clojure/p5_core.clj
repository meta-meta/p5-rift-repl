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
(defn nice-orb [this pG spaceNav]
  (.blendMode pG PApplet/ADD)


  (.noFill pG)
  (.sphereDetail pG 50)

  (.stroke pG 100 50 0 100)
  (.strokeWeight pG 5)


  (.pushMatrix pG)

  (let [[t r] [(.-translation spaceNav) (.-rotation spaceNav)]]
    (let [[x y z] [(.-x t) (.-y t) (.-z t)]]
      (.translateWRTObjectCoords cam (PVector/mult t 0.01))
      (.rotate cam r )
      )
    )

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

(defn p5-drawReplView [this pG spaceNav]
  (.camera cam pG)

  (.background pG 0 0 0 30)
  ;(.background pG 0 50 (* 100 (Math/sin (/ (.millis this) 600))) 20)

  (.sphereDetail pG 5)
  (.fill pG 255 255 255 50)
  (.pushMatrix pG)
    (.translate pG 0 0 5)
    (.sphere pG 0.2)
  (.translate pG 0 3 0)
  (.sphere pG 0.2)
  (.translate pG 2 0 0)
  (.sphere pG 0.2)
  (.translate pG 0 3 -10)
  (.sphere pG 0.2)
  (.popMatrix pG)

  (nice-orb this pG spaceNav)

  )

(defn start []
  (PApplet/main "p5-core.P5ReplClj"))

;(.yaw cam -0.1)
;(start)


