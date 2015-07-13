(ns p5-core
  (:import (processing.core.PApplet)
           (com.generalprocessingunit.hid.megamux ExampleDevice)))

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

(def megamux (ExampleDevice.))

(defmacro push-pop [pG & body]
  (list 'do '(.pushMatrix pG) (cons 'do body) '(.popMatrix pG))
  )

(defn p5-setup [this]
  ;(.size this 800 600 PApplet/OPENGL)
  (.size this (.-displayWidth this) (.-displayHeight this) PApplet/OPENGL)
  (.parentSetup this) ;calls P5Repl.setup()
  )

;TODO: move this and others like it to another file
(defn nice-orb [this pG]
  (.blendMode pG PApplet/ADD)


  (.noFill pG)
  (.sphereDetail pG 50)

  (.stroke pG 100 50 0 100)
  (.strokeWeight pG 5)


  (push-pop pG

            (let [r (mod (/ (.millis this) 2000) PApplet/TWO_PI)]
              (.rotateY pG r)
              )
            (if (> 0.3 (rand)) (.sphere pG 0.6)))


  (.stroke pG 0 50 100 100)


  (push-pop pG
    (let [r (mod (/ (.millis this) 3000) PApplet/TWO_PI)]
      (.rotateX pG r)
      )

    (if (> 0.2 (rand)) (.sphere pG 0.7)))

  (.stroke pG 50 100 50 100)

  (push-pop pG
            (let [r (mod (/ (.millis this) 5000) PApplet/TWO_PI)]
              (.rotateZ pG r)
              )
            (.sphere pG 0.8))

  (.stroke pG 244 255 255 50)

  (push-pop pG
            (let [r (mod (/ (.millis this) 1100) PApplet/TWO_PI)]
              (.rotateX pG r)
              )
            (.sphere pG 0.8))
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

  (defn stem
    ([n heights sides radii] (stem n heights sides radii (repeat 0) (repeat 0)))
    ([n heights sides radii xRots zRots]
     (push-pop pG
       (doall (map (fn [i h r1 r2 xRot zRot]

                     (.rotateX pG xRot)
                     (.rotateZ pG zRot)
                     (cylinder r1 r2 h sides)
                     (.translate pG 0 h 0))

                   (range 0 n) heights radii (drop 1 radii) xRots zRots))))
    )


  (push-pop pG                                              ;LIGHT
            (let [x (.x l) y (.y l) z (.z l)]
              (.translate pG x y z))

            ; light
            (.lightFalloff pG 0.1 0.1 0.0001)
            (.colorMode pG PApplet/RGB)
            (.pointLight pG 255 255 255 3 0 0)

            ; bulb
            (.noStroke pG)
            (.emissive pG 255)
            (.sphere pG 0.2)
            (.emissive pG 0))


  (.ambientLight pG 250 255 255 0 0 0 )
  (.colorMode pG PApplet/HSB)
  (.blendMode pG PApplet/BLEND)


  (.beginShape pG)                                          ;FLOOR
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


  (defn fib [a b] (cons a (lazy-seq (fib b (+ b a)))))      ;MUSHROOM
  (.fill pG 225 50 100)
  (stem 20 (repeat 3) 30 (fib 1 1))

  ;green
  (.fill pG 100 255 100)

  (push-pop pG                                              ;FERN
            (.translate pG 20 0 0)
            (stem 20 (range 2 0 -0.05) 3 (range 3 0.001 -0.1) (range 0 1000 0.06) (repeat 0)))

  (push-pop pG                                              ;PULSEY
            (.translate pG 0 0 20)
            (stem 10 (repeat 3) 3 (repeatedly #(+ 1 (rand 3)))))

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

;(defn getMegamux
;  (let [v (.getInputVal megamux 0)]
;    (.background pG (* 255 (/ (.getInputVal megamux 0) 1024)) 255 150)
;    (println v)
;    v
;    ))

(defn p5-drawReplView [this pG spaceNav keys]
  (.camera cam pG)

  (.blendMode pG PApplet/BLEND)
  (.colorMode pG PApplet/HSB)

  (.background pG 20 20 25)

  (push-pop pG
            (.translate pG 0 -5 20)
            (.box pG 1)

            (push-pop pG
                      (.translate pG 3 2 10)
                      (.box pG 2))
            )

  (if (get keys (Integer. 32))  ;spacebar
    (doRelativeSpaceNav spaceNav l cam)
    (doSpaceNav spaceNav cam)
    )

  (drawStem pG this)
  ;(drawStackOfRings pG this)

  ;(nice-orb pG this)

  )

(defn start []
  (PApplet/main (into-array ["--full-screen" "--display=1" "p5-core.P5ReplClj"])))

;(defn start []
;  (PApplet/main (into-array ["p5-core.P5ReplClj"])))

;(.yaw cam -0.1)
;(start)


