(ns p5-core
  (:use tone-shape)
  (:use music_staff)
  (:import (processing.core.PApplet)
           (com.generalprocessingunit.hid.megamux ExampleDevice)
           (com.illposed.osc OSCListener)
           (com.generalprocessingunit.io OSC)
           (com.generalprocessingunit.processing.space YawPitchRoll)))

(gen-class
  :name p5-core.P5ReplClj
  :extends com.generalprocessingunit.processing.P5ReplDualMon
  :exposes-methods {setup parentSetup}                      ;exposes P5Repl.setup()
  :prefix "p5-")

(import processing.core.PApplet)
(import processing.core.PVector)
(import com.generalprocessingunit.processing.space.Camera)
(import com.generalprocessingunit.processing.space.EuclideanSpaceObject)

(defn resetCam []
  (def camMount (EuclideanSpaceObject.))
  (def cam [(Camera.) (Camera.)])
  (.addChild camMount (first cam))
  (.addChild camMount (last cam) (YawPitchRoll. PApplet/QUARTER_PI 0 0)))
(resetCam)

(def megamux (ExampleDevice.))

(defmacro push-pop [pG & body]
  (list 'do '(.pushMatrix pG) (cons 'do body) '(.popMatrix pG))
  )

(defn p5-setup [this]
  ;(.size this 800 600 PApplet/OPENGL)
  (.size this (.-displayWidth this) (.-displayHeight this) PApplet/OPENGL)
  (.parentSetup this) ;calls P5Repl.setup()
  )



(def ccKeys (let [ccs (flatten [
                                1                           ;modWheel
                                (range 22 27)               ;transportButtons
                                (range 27 34)               ;buttons B44 - B51
                                (range 34 44)               ;buttons B52 - B60
                                (range 75 84)               ;sliders
                                (range 85 104)              ;dials bottom row
                                (range 104 112)             ;dials middle row
                                (range 112 119)             ;dials top row
                                ])]
              (map (fn [i] (str "/cc/" i)) ccs)))

(def sounds (ref {}))
(def cc (ref (zipmap ccKeys (repeat '(0)))))

(defn addListener [address ref]
  (OSC/addListener address
                   (reify OSCListener
                     (acceptMessage [this time msg]
                       (dosync
                         (ref-set ref
                                  (merge
                                    (deref ref)
                                    {address (.getArguments msg)})))
                       ))))

(doall (map addListener
            (flatten (map (fn [i]
                            (map (fn [a] (str "/" i a))
                                 (flatten ["/loudness" "/brightness" "/noisiness" "/bark" "/peaks"
                                           (map (fn [i] (str "/sines/" i)) (range 1 21))])))
                          (range 0 8)
                          ))
            (repeat sounds)
            ))

(doall (map addListener ccKeys (repeat cc)))



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

(defn drawStackOfRings [pG p5 sounds cc]
  (defn stacky [n t radii]
    (.colorMode pG PApplet/HSB)
    (.blendMode pG PApplet/ADD)

    (defn drawSphere [x y hue bright]

      (.noFill pG)
      (.strokeWeight pG (rand 3))
      (.stroke pG 255)
      (.fill pG hue 255 bright 100)
      (.sphereDetail pG (+ 3 (rand 5)))

      (.pushMatrix pG)
      (.translate pG (* x 0.1) 0 (* y 0.1))
      (.sphere pG (rand (* 0.003 bright)))
      (.popMatrix pG)
      )

    (defn circleOfSpheres [n r]
      (let [thetas (map (fn [x] (* x (/ PApplet/TWO_PI n))) (range 0 n))
            xs (map (fn [theta] (* r (Math/sin theta))) thetas)
            zs (map (fn [theta] (* r (Math/cos theta))) thetas)
            brights (map
                      (fn [a] (* 25500 (last (get sounds (str "/0/sines/" (+ 1 a))))))
                      (range 0 n))
            hues (map
                   (fn [a] (mod (* 0.5 (first (get sounds (str "/0/sines/" (+ 1 a))))) 255))
                   (range 0 n))]
        (doall (map drawSphere xs zs hues brights))
        )
      )

    (defn drawCircle [i delta r]
      (.pushMatrix pG)
      (.translate pG 0 (* i delta) 0)
      (.rotateY pG (*
                     (if (== (mod i 2) 0) 1 -1)
                     (* (.millis p5)
                        (first (get cc "/cc/77"))
                        0.00001)))
      (circleOfSpheres 20 r)
      (.popMatrix pG))

    (doall (map drawCircle (range 0 n) (repeat t) radii))
    )

  (stacky 10 1 (range 90 1000 0.2))

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
            (let [bright (first (get (deref sounds) "/0/brightness"))]
              ;(println bright)
              (stem 30
                    (range 2 0 -0.05)
                    3
                    (range 3 0.001 -0.1)
                    (range 0 1000 (+ 0.02 (* 0.000005 bright)))
                    (repeat 0))))

  (push-pop pG                                              ;PULSEY
            (.translate pG 0 0 20)
            (let [noiz (first (get (deref sounds) "/0/noisiness"))]
              (stem 10 (repeat 3) 3 (repeatedly #(+ 1 (rand (* 2 noiz)))))))

  )

(defn doSpaceNav [spaceNav obj]
  (let [[t r] [(.getTranslation spaceNav) (.getRotation spaceNav)]]
    (let [[x y z] [(.-x t) (.-y t) (.-z t)]]
      (.translateWRTObjectCoords obj (PVector/mult t 0.05))
      (.rotate obj r)
      )))

(defn doRelativeSpaceNav [spaceNav obj relTo]
  (let [[t r] [(.getTranslation spaceNav) (.getRotation spaceNav)]]
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
;

; TODO detect condition, set flag with hash of condition and action, detect !condition set flag false
(defn trigger [condition action])


(defn spaceNavSettings [spaceNav]
  (.setCoefTranslation spaceNav 0.1)
  (.setFrictionTranslation spaceNav 0.0007)
  (.setCoefRotation spaceNav 0.005)
  (.setFrictionRotation spaceNav 0.007)
  )

(defn p5-drawReplView [this pG mon spaceNav keys scroll]
  (set! (.-fov (nth cam mon)) (Math/max 0.001 (* (* scroll 0.01) 1.12)))
  (.camera (nth cam mon) pG)
  (spaceNavSettings spaceNav)


  (.blendMode pG PApplet/BURN)
  ;(.blendMode pG PApplet/ADD)

  ;(.blendMode pG PApplet/BLEND)
  (.colorMode pG PApplet/HSB)

  (let [cc (deref cc)
        h1 (int (* 2 (first (get cc "/cc/87"))))
        s1 (int (* 2 (first (get cc "/cc/106"))))
        b1 (int (* 2 (first (get cc "/cc/114"))))
        h2 (int (* 2 (first (get cc "/cc/88"))))
        s2 (int (* 2 (first (get cc "/cc/107"))))
        b2 (int (* 2 (first (get cc "/cc/115"))))]
    (if (= mon 0)
      (.background pG h1 s1 b1 100)
      (.background pG h2 s2 b2 100))
    )

  (.setAlwaysOnTop (.-frame this) true)
  ;(.noCursor this)
  ;(.cursor this PApplet/ARROW)

  (if (get keys (Integer. 38))
    (println scroll))


  ;(push-pop pG
  ;          (.translate pG 0 -5 20)
  ;          (.box pG 1)
  ;
  ;          (push-pop pG
  ;                    (.translate pG 3 2 10)
  ;                    (.box pG 2))
  ;          )

  ;(println keys)
  (if (get keys (Integer. 32))  ;spacebar
    (doRelativeSpaceNav spaceNav l camMount)
    (if (get keys (Integer. 37))
      (doSpaceNav spaceNav (nth cam 0))
      (if (get keys (Integer. 39))
        (doSpaceNav spaceNav (nth cam 1))
        (doSpaceNav spaceNav camMount)
        )
      )
    )

  (let [cc (deref cc)
        sounds (deref sounds)
        a (int (first (get cc "/cc/27")))
        b (int (first (get cc "/cc/28")))
        c (int (first (get cc "/cc/29")))
        d (int (first (get cc "/cc/30")))
        e (int (first (get cc "/cc/31")))
        f (int (first (get cc "/cc/32")))]
    (if (= a 127)
      (tone-shape/draw this pG sounds cc)
      (if (= b 127)
        (drawStackOfRings pG this sounds cc)
        (if (= c 127)
          (drawStem pG this)
          )
        )
      )
    )

  ;(tone-shape/draw this pG (deref sounds) (deref cc))
  ;(music_staff/drawme this pG (deref sounds))

  ;(drawStem pG this)
  ;(drawStackOfRings pG this)

  ;(nice-orb pG this)

  )

(defn start []
  (PApplet/main (into-array ["--display=2" "--full-screen" "p5-core.P5ReplClj"])))

;(defn start []
;  (PApplet/main (into-array ["p5-core.P5ReplClj"])))

;(.yaw cam -0.1)
;(start)


