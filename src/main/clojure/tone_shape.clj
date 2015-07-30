(ns tone-shape
  (:import (processing.core PApplet)))

(import processing.core.PApplet)

(defn draw [this pG sounds cc track]

  (defn getProp [propName]
    (first (get sounds (str "/" track "/" propName))))

  (defn getPitch []
    (first (get sounds (str "/" track "/pitch"))))

  (defn getCC [ccNum]
    (first (get cc (str "/cc/" ccNum))))

  (defn getSine [n]
    (get sounds (str "/" track "/sines/" n)))

  ;(let [h (* 0.01 (getProp "brightness"))
  ;      s (* 200 (getProp "noisiness"))
  ;      b (- 0 (* 0.4 (getProp "loudness")))
  ;      a 80]
  ;  ;(.background pG h s b a)
  ;  ;(println b)
  ;  )

  (doall (map (fn [nSine]
                (.pushMatrix pG)
                (let [sine (getSine nSine)
                      millis (.millis this)
                      m (mod (/ millis 10) 255)
                      s (* 15 (Math/sin (mod (+ (* 0.001 millis) nSine) PApplet/TWO_PI)))
                      c (* 2 (Math/cos (mod (+ (* 0.001 millis) nSine) PApplet/TWO_PI)))
                      ]

                  (doall (map (fn [y]
                                (.pushMatrix pG)

                                (if (= 0 y)
                                  (.fill pG                 ;base pad for meter
                                         (* 2 (getCC 85))
                                         (* 2 (getCC 104))
                                         (* 2 (getCC 112))
                                         200)
                                  (.fill pG                 ;rest of meter
                                         (mod (+ m (* 32 y)) 255)
                                         200
                                         200
                                         (+ 30 (* 1000 (last sine))))
                                  )


                                (.translate pG (* 0.03 (first sine)) (+ c (* 6 y)) s)


                                (let [                      ;rando lines spikey ----
                                      l (int (*
                                               (* 10 (getCC 75))
                                               (getProp "loudness")
                                               (getProp "noisiness")))
                                      sh (int (* 2 (getCC 86)))
                                      ss (int (* 2 (getCC 105)))
                                      sb (int (* 2 (getCC 113)))
                                      s (int (* 2 (getCC 76)))
                                      rx (- (rand (* 2 l)) l)
                                      ry (- (rand (* 2 l)) l)
                                      rz (- (rand (* 2 l)) l)
                                      ]
                                  (.stroke pG (+ sh (- (rand 90) 45)) ss sb (rand 100))
                                  (.strokeWeight pG (rand (* 0.1 s)))
                                  (.line pG 0 0 0 rx ry rz)
                                  )


                                (.strokeWeight pG 0.1)
                                (.stroke pG 20 255 255 (+ 100 (* 150 (last sine)))) ;meters
                                (.scale pG 150 200 50)
                                (.sphereDetail pG 4)
                                (.sphere pG 0.1)
                                (.popMatrix pG)

                                ) (range 0 (* 10 (last sine)) 0.4)))
                  )
                (.popMatrix pG)
                )

              (range 1 11)
              )
         )

  )

