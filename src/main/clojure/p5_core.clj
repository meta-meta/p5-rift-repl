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
  (.size this 800 800 PApplet/OPENGL)
  (.parentSetup this) ;calls P5Repl.setup()
  )

;TODO: move this and others like it to another file
(defn nice-orb [this pG spaceNav]
  (.blendMode pG PApplet/ADD)


  (.noFill pG)
  (.sphereDetail pG 50)

  (.stroke pG 255 0 0 10)
  (.strokeWeight pG 200)


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
  (.sphere pG 0.6)

  (.popMatrix pG)

  (.stroke pG 0 0 200 10)


  (.pushMatrix pG)
  (let [r (mod (/ (.millis this) 3000) PApplet/TWO_PI)]
    (.rotateX pG r)
    )
  (.sphere pG 0.7)
  (.popMatrix pG)

  (.stroke pG 0 255 0 10)

  (.pushMatrix pG)
  (let [r (mod (/ (.millis this) 5000) PApplet/TWO_PI)]
    (.rotateZ pG r)
    )
  (.sphere pG 0.8)
  (.popMatrix pG)

  (.stroke pG 244 255 255 10)

  (.pushMatrix pG)
  (let [r (mod (/ (.millis this) 1100) PApplet/TWO_PI)]
    (.rotateX pG r)
    )
  (.sphere pG 0.8)
  (.popMatrix pG)
  )

(defn p5-drawReplView [this pG spaceNav]
  (.camera cam pG)

  (.background pG 20 50 (* 100 (Math/sin (/ (.millis this) 600))))

  (nice-orb this pG spaceNav)

  )

(defn start []
  (PApplet/main "p5-core.P5ReplClj"))

;(.yaw cam -0.1)
;(start)


