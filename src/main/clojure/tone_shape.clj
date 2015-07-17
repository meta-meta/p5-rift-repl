(ns tone-shape
  (:import (processing.core PApplet)))

(import processing.core.PApplet)


(defn draw [this pG sounds]

  (let [h (* 0.01 (first (get sounds "/brightness")))
        s (* 200 (first (get sounds "/noisiness")))
        b (- 0 (* 0.4 (first (get sounds "/loudness"))))
        a 80]
    (.background pG h s b a)
    ;(println b)
    )



  (doall (map (fn [tuple]
                (.pushMatrix pG)
                (let [sines (get sounds (first tuple))
                      millis (.millis this)
                      m (mod (/ millis 10) 255)
                      s (* 15 (Math/sin (mod (+ (* 0.001 millis) (last tuple)) PApplet/TWO_PI)))
                      c (* 2 (Math/cos (mod (+ (* 0.001 millis) (last tuple)) PApplet/TWO_PI)))
                      loud (+ 50 (* 1 (first (get sounds "/loudness"))))
                      ]

                  (doall (map (fn [y]
                                (.pushMatrix pG)

                                (if (= 0 y)
                                  (.fill pG 170 180 50 200)
                                  (.fill pG (mod (+ m (* 32 y)) 255) 200 200 (+ 30 (* 1000 (last sines))))
                                  )


                                (.translate pG (* 0.03 (first sines)) (+ c (* 6 y)) s)
                                (let [
                                      r (int (* 40 (first (get sounds "/noisiness")) (first (get sounds "/noisiness"))))
                                      rx (- (rand (* 2 r)) r)
                                      ry (- (rand (* 2 r)) r)
                                      rz (- (rand (* 2 r)) r)
                                      ]
                                  (.stroke pG  r)
                                  (.strokeWeight pG (* 0.1 r))
                                  (.line pG 0 0 0 rx ry rz)
                                  )
                                (.stroke pG 140 100 255 (+ 1 (* 100 (last sines))))


                                (.scale pG 10 1 10)
                                (.sphereDetail pG 4)
                                (.sphere pG 0.1)
                                (.popMatrix pG)

                                ) (range 0 (* 100 (last sines)) 0.05)))
                  )
                (.popMatrix pG)
                )
              (map (fn [i] [(str "/sines/" i) i]) (range 1 21))))

  )

