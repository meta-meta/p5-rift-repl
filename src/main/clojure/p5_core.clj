(ns p5-core
  (:use [tone-shape :only [draw]])
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

(defmacro push-pop [pG & body]
  (list 'do '(.pushMatrix pG) (cons 'do body) '(.popMatrix pG))
  )

(defn setCam []
  (def camMount (EuclideanSpaceObject.))
  (def cam [(Camera.) (Camera.)])
  (.addChild camMount (first cam) (PVector. 0 0 -10))
  (.addChild camMount (last cam) (PVector. 0 0 -10)))
(setCam)


(defn resetCam [camNum]
  (let [cam (nth cam camNum)]
    (.setLocation cam 0 0 0)
    (.setOrientation cam (YawPitchRoll.))
    ))

(defn resetCams []
  (resetCam 0)
  (resetCam 1)
  (.adjustChildren camMount)
  )

(defn resetCamMount []
  (.setLocation camMount 0 0 0)
  (.setOrientation camMount (YawPitchRoll.))
  )




(defn p5-setup [this]
  ;(.size this 1280 800 PApplet/OPENGL)
  (.size this (.-displayWidth this) (.-displayHeight this) PApplet/OPENGL)

  (.parentSetup this) ;calls P5Repl.setup()
  )

;(def megamux (ExampleDevice.))



;; OSC Listeners

(defn setref [ref val]
  (dosync
    (ref-set ref val)))

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

(defn getCC [ccNum]
  (first (get (deref cc) (str "/cc/" ccNum))))

(defn addListener [address ref]
  (OSC/addListener address
                   (reify OSCListener
                     (acceptMessage [this time msg]
                       (setref ref
                               (merge
                                 (deref ref)
                                 {address (.getArguments msg)}))
                       ))))

(let [soundAddrs (flatten (map (fn [i]
                                 (map (fn [a] (str "/" i a))
                                      (flatten ["/loudness" "/brightness" "/noisiness" "/bark" "/peaks" "/pitch"
                                                (map (fn [i] (str "/sines/" i)) (range 1 21))])))
                               (range 1 9)
                               ))]
  (setref sounds (zipmap soundAddrs (repeat '(0 0))))

  (doall (map addListener
              soundAddrs (repeat sounds)))
  )



(doall (map addListener ccKeys (repeat cc)))







;TODO: move this and others like it to another file
(defn nice-orb [pG p5 sounds cc]
  (defn getProp [track propName]
    (first (get sounds (str "/" track "/" propName))))

  (defn getSine [track n]
    (get sounds (str "/" track "/sines/" n)))

  (let [h (* 2 (getCC 82))]
    (.noFill pG)
    (.sphereDetail pG (getCC 80))
    (.strokeWeight pG (* 0.4 (getCC 81)))


    (defn hue [in]
      (mod (+ in h) 255)
      )

    (push-pop pG
              (.rotateY pG (mod (/ (.millis p5) 2000) PApplet/TWO_PI))
              (.stroke pG
                       (hue 255)
                       0
                       255
                       (* 0.005 (getProp 1 "brightness")))
              (.sphere pG 60))


    (push-pop pG
              (.rotateX pG (mod (/ (.millis p5) 3000) PApplet/TWO_PI))
              (.stroke pG
                       (hue 0)
                       255
                       255
                       (* 500 (- (getProp 1 "noisiness") 0.7)))
              (.sphere pG 80))



    (push-pop pG
              (.rotateZ pG (mod (/ (.millis p5) 5000) PApplet/TWO_PI))
              (.stroke pG
                       (hue 70)
                       255
                       200
                       60)
              (.sphere pG 100)

              (let [
                    
                    s1 (getSine 2 1)
                    s1Freq (first s1)
                    s1Amp (last s1)

                    s2 (getSine 2 2)
                    s2Freq (first s2)
                    s2Amp (last s2)
                    
                    ]

                (.stroke pG (mod (* 255 s1Amp) 255) 255 255)
                (.strokeWeight pG (* 10 s1Amp))
                (.line pG
                       (* 80 (Math/sin s1Freq))
                       (* 80 (Math/cos s1Freq))
                       (* 80 (Math/cos s1Amp))
                       (* 100 (Math/sin s1Freq))
                       (* 100 (Math/cos s1Freq))
                       100)

                (.stroke pG (mod (* 255 s2Amp) 255) 255 255)
                (.strokeWeight pG (* 10 s2Amp))
                (.line pG
                       (* 80 (Math/sin s2Freq))
                       (* 80 (Math/cos s2Freq))
                       (* 80 (Math/cos s2Amp))
                       (* 100 (Math/sin s2Freq))
                       (* 100 (Math/cos s2Freq))
                       100)
                
                )
              
              )

    (push-pop pG
              (.rotateX pG (mod (/ (.millis p5) 1100) PApplet/TWO_PI))
              (.stroke pG
                       (hue 180)
                       255
                       150
                       60)
              (.sphere pG 300)



              

              ))
  ;
  ;(.stroke pG 244 255 255 100)
  ;
  ;(push-pop pG
  ;          (let [r (mod (/ (.millis p5) 1100) PApplet/TWO_PI)]
  ;            (.rotateX pG r)
  ;            )
  ;          (.sphere pG 80))
  )




(defn drawStackOfRings [pG p5 sounds cc track]
  ;TODO: helper class
  (defn getProp [propName]
    (first (get sounds (str "/" track "/" propName))))

  (defn getPitch []
    (first (get sounds (str "/" track "/pitch"))))

  (defn getSine [n]
    (get sounds (str "/" track "/sines/" n)))

  (defn stacky [n t radii]
    (.colorMode pG PApplet/HSB)

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
                      (fn [a]
                        (* 25500
                           (last (getSine (+ 1 a)))))
                      (range 0 n))
            hues (map
                   (fn [a]
                     (mod (*
                            0.5
                            (first (getSine (+ 1 a))))
                          255))
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
                        (getCC 77)
                        0.00001)))
      (circleOfSpheres 20 r)
      (.popMatrix pG))

    (doall (map drawCircle (range 0 n) (repeat t) radii))
    )

  (stacky 10 5 (range 500 1000 0.2))

  )





(defn drawTvNoise [pG track]
  (defn getProp [propName]
    (first (get (deref sounds) (str "/" track "/" propName))))

  ;TODO: macro for 2D drawing
  (.hint pG PApplet/DISABLE_DEPTH_MASK)
  (.camera pG)
  (.perspective pG)

  (let [size (+ 20 (* 10 (getCC 79)))]

    (doall (for [x (range 0 (.-width pG) size)
                 y (range 0 (.-height pG) size)
                 :let [r (<
                           (rand)
                           (* size 0.001 (getProp "noisiness") (getProp "noisiness") (getProp "noisiness")))]
                 :when r]
             (do
               (.fill pG
                      255
                      0
                      (rand 255)
                      (* 2 (getCC 78)))
               (.stroke pG
                      255
                      0
                      (rand 255)
                      (* 2 (getCC 78)))
               (.rect pG
                      x y
                      size size)
               )
             )))

  (.hint pG PApplet/ENABLE_DEPTH_MASK)
  )




(def l (EuclideanSpaceObject.))

(defn drawStem [pG p5 track]

  (defn getProp [propName]
    (first (get (deref sounds) (str "/" track "/" propName))))

  (defn getPitch []
    (first (get (deref sounds) (str "/" track "/pitch"))))

  (defn getSine [n]
    (get (deref sounds) (str "/" track "/sines/" n)))

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

            (let [loud (* 2 (+ 100 (getProp "loudness")))]
              ; light
              (.lightFalloff pG 0.1 0.1 0.001)
              (.colorMode pG PApplet/RGB)
              (.pointLight pG
                           255
                           loud
                           255 3 0 0)

              ; bulb
              (.noStroke pG)
              (.emissive pG 255)
              (.fill pG loud loud loud loud)

              (.sphere pG 0.2)
              (.emissive pG 0)

              (.ambientLight pG 250 loud loud 0 0 0))
            )





  (.colorMode pG PApplet/HSB)
  ;(.blendMode pG PApplet/BLEND)


  ;(.beginShape pG)                                          ;FLOOR
  ;(.emissive pG (.color p5 20 0 0))
  ;(.vertex pG -1000 0 -1000)
  ;(.emissive pG (.color p5 50 0 0))
  ;(.vertex pG -1000 0 1000)
  ;(.emissive pG (.color p5 0 50 0))
  ;(.vertex pG 1000 0 1000)
  ;(.emissive pG (.color p5 50 50 0))
  ;(.vertex pG 1000 0 -1000)
  ;(.endShape pG)
  ;(.emissive pG 0)


  (.stroke pG 50 255 0)
  (.strokeWeight pG 0.5)





  (defn mushroom []
    (defn fib [a b] (cons a (lazy-seq (fib b (+ b a)))))      ;MUSHROOM
    (.fill pG 225 50 100 50)
    (stem 10 (repeat 3) 10 (fib 1 1))
    )

  (mushroom)

  (push-pop pG
            (.scale pG 1 -1 1)
            (mushroom))

  ;green
  (.fill pG 100 255 100)

  (defn fern []
    (push-pop pG                                              ;FERN
              (.translate pG 20 0 0)
              (let [bright (getProp "brightness")]
                ;(println bright)
                (stem 30
                      (range 2 0 -0.05)
                      3
                      (range 3 0.001 -0.1)
                      (range 0 1000 (+ 0.02 (* 0.000009 bright)))
                      (repeat 0))))
    )

  (fern)
  (push-pop pG
            (.scale pG 1 -1 1)
            (fern))

  (defn pulsey []
    (push-pop pG                                              ;PULSEY
              (.translate pG 0 0 20)
              (let [noiz (getProp "noisiness")]
                (stem 10 (repeat 3) 3 (repeatedly #(+ 1 (rand (* 2 noiz)))))))
    )
  (pulsey)
  (push-pop pG
            (.scale pG 1 -1 1)
            (pulsey))
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
  (.setCoefRotation spaceNav 0.008)
  (.setFrictionRotation spaceNav 0.0009)
  )

(defn p5-drawReplView [this pG mon spaceNav keys scroll]
  (.setAlwaysOnTop (.-frame this) true)
  ;(.noCursor this)
  ;(.cursor this PApplet/ARROW)

  ;; CAMERA
  (set! (.-fov (nth cam mon)) (Math/max 0.001 (* (* scroll 0.01) 1.12)))
  (.camera (nth cam mon) pG)
  (spaceNavSettings spaceNav)


  ;; BLEND AND COLORS
  (let [modwheel (getCC 1)
        region (/ 127 4)]
    (cond
      (< modwheel region)
      (.blendMode pG PApplet/DARKEST)
      (< modwheel (* 2 region))
      (.blendMode pG PApplet/BLEND)
      (< modwheel (* 3 region))
      (.blendMode pG PApplet/SCREEN)
      (< modwheel (* 4 region))
      (.blendMode pG PApplet/ADD)
      )
    )

  ;(.blendMode pG PApplet/BLEND)

  (.colorMode pG PApplet/HSB)

  ;; BACKGROUND
  (if (= mon 0)
    (.background pG
                 (* 2 (getCC 87 ))
                 (* 2 (getCC 106 ))
                 (* 2 (getCC 114 ))
                 (- 255 (* 2 (getCC 83))))
    (.background pG
                 (* 2 (getCC 88 ))
                 (* 2 (getCC 107 ))
                 (* 2 (getCC 115 ))
                 (- 255 (* 2 (getCC 83)))))



  ;; KEYBOARD
  (if (get keys (Integer. 38))
    (println scroll))


  ;(println keys)
  (cond
    (get keys (Integer. 32))
    (doRelativeSpaceNav spaceNav l camMount)

    (get keys (Integer. 37))
    (doSpaceNav spaceNav (nth cam 0))

    (get keys (Integer. 39))
    (doSpaceNav spaceNav (nth cam 1))

    true
    (doSpaceNav spaceNav camMount)
    )


  (defn drawToneShape [sounds cc]
    (tone-shape/draw this pG sounds cc 1)
    (push-pop pG
              (.translate pG 150 0 0)
              (.rotateY pG PApplet/HALF_PI)
              (tone-shape/draw this pG sounds cc 2)
              )
    )



  ;; SELECT SCENE
  ;(let [cc (deref cc)
  ;      sounds (deref sounds)]
  ;
  ;  (cond
  ;    (= 127 (getCC 27))
  ;    (drawToneShape sounds cc)
  ;
  ;    (= 127 (getCC 28))
  ;    (drawStackOfRings pG this sounds cc 2)
  ;
  ;    (= 127 (getCC 29))
  ;    (nice-orb pG this sounds cc)
  ;
  ;    (= 127 (getCC 30))
  ;    (drawStem pG this (+ 1 mon))
  ;
  ;    )
  ;
  ;  )


  (defn drawPitches [sounds]
    (defn getProp [track propName]
      (first (get sounds (str "/" track "/" propName))))

    (let [pitch (getProp 2 "pitch")]
      (.hint pG PApplet/DISABLE_DEPTH_MASK)
      (.camera pG)
      (.perspective pG)

      ;(.background pG 255)
      (.fill pG
             (mod (+ (* 127 mon) (* 2 (getCC 89))) 255)
             (* 2 (getCC 108))
             (* 2 (getCC 116)))

      (.stroke pG 255)
      (.rect pG
             (mod (- (* mon (.-width pG)) (* 100 (- pitch 60))) (.-width pG))
             0
             100
             (.-height pG))

      ;(push-pop pG
      ;
      ;          )

      (.hint pG PApplet/ENABLE_DEPTH_MASK)
      )
    )

  (defn drawMoog [sounds]
    (defn getProp [track propName]
      (first (get sounds (str "/" track "/" propName))))

    (let [pitch (getProp 2 "pitch")
          loudness (* 1 (+ 100 (getProp 1 "loudness")))
          noisiness (getProp 1 "noisiness")
          ]
      (.hint pG PApplet/DISABLE_DEPTH_MASK)
      (.camera pG)
      (.perspective pG)

      (.fill pG
             (mod (+ (* 127 mon) (* 2 (getCC 90))) 255)
             (* 2 (getCC 109))
             (* 2 (getCC 117))
             50
             )

      (.sphereDetail pG (- 30 (* 30 noisiness)))
      (.strokeWeight pG (- 10 (* 10 noisiness)))

      ;(.noStroke pG)
      (.stroke pG
               (mod (+ (* 127 mon) (* 2 (getCC 90))) 255)
               (* 2 (getCC 109))
               (* 2 (getCC 117))
               100)
      (push-pop pG
                (.translate pG
                  (mod (* 30 pitch) (.-width pG))
                  (* 0.5 (.-height pG))
                  0)

                (.rotateY pG (* 0.001 (.millis this)))

                (.sphere pG 200)

                )

      (.hint pG PApplet/ENABLE_DEPTH_MASK)
      )
    )

  (if (= 127 (getCC 31))
    (drawPitches (deref sounds)))

  (if (= 127 (getCC 32))
    (drawMoog (deref sounds)))


  (defn drawSpectra [sounds]
    (defn getSine [track n]
      (get sounds (str "/" track "/sines/" n)))

    (defn getProp [track propName]
      (first (get sounds (str "/" track "/" propName))))

    (defn log2 [n] (/ (Math/log n) (Math/log 2)))

    ;(defn ftom [freq]
    ;  (+
    ;    69
    ;    (* 12
    ;       (log2 (/
    ;               freq
    ;               440)))))

    (defn freq-to-radian [freq]
      (* (PApplet/TWO_PI)
         (log2 (/
                 freq
                 440))))

    (.background pG 0 0 20 5)

    (.blendMode pG PApplet/ADD)
    (.noStroke pG)
    ;(.noFill pG)
    (.fill pG 255 50 50 100)
    (.sphereDetail pG 4)
    ;(.sphere pG 1)

    (let [
          sines (filter
                  (fn [sine] (> (first sine) 0))
                  (map
                    (partial getSine 1)
                    (range 20 0 -1)))]

      (doall (map (fn [sine]
                    (let
                      [freq (first sine)
                       amp (last sine)
                       theta (freq-to-radian freq)
                       r 3
                       y (/ theta PApplet/TWO_PI)
                       octave (quot theta PApplet/TWO_PI)]

                      (push-pop pG
                                (.translate pG
                                            (* r (- 7 y) (Math/sin (+ (* 0.0001 (.millis this)) theta)))
                                            (* 10 y)
                                            (* r (- 7 y) (Math/cos (+ (* 0.0001 (.millis this)) theta))))
                                (.strokeWeight pG (- 4 (* 2 y)))
                                (.stroke pG
                                         (* (mod theta PApplet/TWO_PI)
                                            (/ 255 PApplet/TWO_PI))
                                         255
                                         255
                                         (* amp 450))
                                (.sphereDetail pG (- 7 y))
                                (.sphere pG (- 7 y))
                                )

                      )
                    ) sines))

      )

    )

  (defn getkey [keycode] (get keys (Integer. keycode)))

  (defn isCC [cc] (= 127 (getCC cc)))

  (let [cc (deref cc)
        sounds (deref sounds)]

    ;(cond
    ;  (getkey 49)
    ;  (drawSpectra sounds)
    ;
    ;  (getkey 50)
    ;  (drawToneShape sounds cc)
    ;
    ;  (getkey 51)
    ;  (drawStackOfRings pG this sounds cc 1)
    ;
    ;  (getkey 52)
    ;  (nice-orb pG this sounds cc)
    ;
    ;  (getkey 53)
    ;  (drawStem pG this 1)
    ;
    ;  )

    (cond
      (isCC 27)
      (drawSpectra sounds)

      (isCC 28)
      (drawToneShape sounds cc)

      (isCC 29)
      (drawStackOfRings pG this sounds cc 1)

      (isCC 30)
      (nice-orb pG this sounds cc)

      (isCC 31)
      (drawStem pG this 1)

      )

    )

  (cond
    (getkey 55)
    (drawTvNoise pG 1)

    (getkey 56)
    (drawPitches (deref sounds))

    (getkey 57)
    (drawMoog (deref sounds))

    )


  ;(drawTvNoise pG 1)


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


