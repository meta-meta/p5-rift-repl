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
(import com.generalprocessingunit.processing.space.EuclideanSpaceObject)

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

(defn drawStackOfRings [pG p5]
  (defn stacky [n t radii]
    (.colorMode pG PApplet/HSB)
    (.blendMode pG PApplet/ADD)

    (defn drawSphere [x y hue]

      (.noFill pG)
      (.strokeWeight pG (rand 10))
      (.stroke pG hue 255 255 60)
      (.sphereDetail pG (+ 5 (rand 3)))

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
      (.rotateY pG (* (if (== (mod i 2) 0) 1 -1) (/ (.millis p5) 1000)))
      (circleOfSpheres 12 r)
      (.popMatrix pG))

    (doall (map drawCircle (range 0 n) (repeat t) radii))
    )

  (stacky 10 0.07 (range 2 1000 0.5))

  )

(def l (EuclideanSpaceObject.))

(defn drawStem [pG p5]
  (defn cylinder [r1 r2 h sides]
    (.beginShape pG PApplet/QUAD_STRIP)

    (let [thetas (map (fn [a b] (* a b)) (range 0 (+ 1 sides)) (repeat (/ PApplet/TWO_PI sides)))
          xs1 (map (fn [theta] (* r1 (Math/sin theta))) thetas)
          zs1 (map (fn [theta] (* r1 (Math/cos theta))) thetas)
          xs2 (map (fn [theta] (* r2 (Math/sin theta))) thetas)
          zs2 (map (fn [theta] (* r2 (Math/cos theta))) thetas)]
      (doall (map (fn [x1 z1 x2 z2]
                    (.vertex pG x1 0 z1)
                    (.vertex pG x2 h z2)
                    ) xs1 zs1 xs2 zs2)))

    (.endShape pG)
    )

  ;(cylinder 0.1 0.5 5)

  (defn stem
    ([n heights sides radii] (stem n heights sides radii (repeat 0) (repeat 0)))
    ([n heights sides radii xRots zRots]
     (.pushMatrix pG)
     (doall (map (fn [i h r1 r2 xRot zRot]

                   (.rotateX pG xRot)
                   (.rotateZ pG zRot)
                   (cylinder r1 r2 h sides)
                   (.translate pG 0 h 0))

                 (range 0 n) heights radii (drop 1 radii) xRots zRots))
     (.popMatrix pG))
    )


  (.pushMatrix pG)
  (let [x (.x l) y (.y l) z (.z l)]
    (.translate pG x y z))
  (.lightFalloff pG 0.1 0.1 0.0001)
  (.colorMode pG PApplet/RGB)

  (.pointLight pG 255 255 255 3 0 0)

  (.emissive pG 255)
  (.sphere pG 0.2)
  (.emissive pG 0)
  (.popMatrix pG)

  (.ambientLight pG 250 255 255 0 1 0 )

  (.colorMode pG PApplet/HSB)
  (.blendMode pG PApplet/BLEND)


  (.beginShape pG)
  (.emissive pG (.color p5 20 0 0))
  (.vertex pG -1000 0 -1000)
  (.emissive pG (.color p5 50 0 0))
  (.vertex pG -1000 0 1000)
  (.emissive pG (.color p5 0 50 0))
  (.vertex pG 1000 0 1000)
  (.emissive pG (.color p5 50 50 0))
  (.vertex pG 1000 0 -1000)
  (.endShape pG)
  (.emissive pG 0)


  (.stroke pG 50 255 0)
  (.strokeWeight pG 0.5)


  (.fill pG 225 255 100)


  (defn fib [a b] (cons a (lazy-seq (fib b (+ b a)))))
  (stem 20 (repeat 3) 30 (fib 1 1))

  (.fill pG 100 255 100)


  ;(stem 30 3 3 (repeatedly #(+ 1 (rand 2))))

  (.translate pG 20 0 0)
  (stem 20 (range 2 0 -0.05) 3 (range 3 0.001 -0.1) (range 0 1000 0.06) (repeat 0))

  (.translate pG 0 0 20)
  (stem 10 (repeat 3) 3 (repeatedly #(+ 1 (rand 3))))

  )

(defn doSpaceNav [spaceNav obj]
  (let [[t r] [(.-translation spaceNav) (.-rotation spaceNav)]]
    (let [[x y z] [(.-x t) (.-y t) (.-z t)]]
      (.translateWRTObjectCoords obj (PVector/mult t 0.1))
      (.rotate obj r)
      )))

(defn doRelativeSpaceNav [spaceNav obj relTo]
  (let [[t r] [(.-translation spaceNav) (.-rotation spaceNav)]]
    (let [[x y z] [(.-x t) (.-y t) (.-z t)]]
      (.translateObjWRTObjectCoords relTo (PVector/mult t 0.1) obj)
      (.rotate obj r)
      )))

(defn p5-drawReplView [this pG spaceNav]
  (.camera cam pG)

  (.background pG 20 20 60 255)

  (doSpaceNav spaceNav cam)
  ;(doRelativeSpaceNav spaceNav l cam)

  (drawStem pG this)

  ;(drawStackOfRings pG this)

  )

(defn start []
  (PApplet/main "p5-core.P5ReplClj"))

;(.yaw cam -0.1)
;(start)


