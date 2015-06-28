(ns p5-core
  (:import (processing.core.PApplet)))

(gen-class
  :name p5-core.P5ReplClj
  :extends com.generalprocessingunit.processing.P5Repl
  :exposes-methods {setup parentSetup}                      ;exposes P5Repl.setup()
  :prefix "p5-")

(import processing.core.PApplet)
(import com.generalprocessingunit.processing.space.Camera)
(import com.generalprocessingunit.hid.SpaceNavigator)

(def cam (new Camera))
(def spaceNav (new SpaceNavigator this))

(defn p5-setup [this]
  (.size this 800 800 PApplet/OPENGL)
  (.parentSetup this) ;calls P5Repl.setup()
  )

(defn p5-drawReplView [this pG]
  (.camera cam pG)

  (.background pG 127 10 (* 200 (Math/sin (/ (.millis this) 600))))

  (.fill pG 0 0 0 0)
  (.stroke pG 255)
  (.pushMatrix pG)

  (.translate pG 0 0.2 5)

  (.rotateX pG -0.5)

  (let [r (mod (/ (.millis this) 1000) PApplet/PI)]
    (.rotateY pG r)
    )
  (.sphere pG 0.6)

  (.popMatrix pG)

  )

(defn start []
  (PApplet/main "p5-core.P5ReplClj"))

;(.yaw cam -0.1)