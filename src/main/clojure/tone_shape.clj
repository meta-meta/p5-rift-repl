(ns tone-shape
  (:import (processing.core PApplet)))

(import processing.core.PApplet)


(defn draw [this pG sounds cc]

  (let [h (* 0.01 (first (get sounds "/0/brightness")))
        s (* 200 (first (get sounds "/0/noisiness")))
        b (- 0 (* 0.4 (first (get sounds "/0/loudness"))))
        a 80]
    ;(.background pG h s b a)
    ;(println b)
    )



  (doall (map (fn [tuple]
                (.pushMatrix pG)
                (let [sines (get sounds (first tuple))
                      millis (.millis this)
                      m (mod (/ millis 10) 255)
                      s (* 15 (Math/sin (mod (+ (* 0.001 millis) (last tuple)) PApplet/TWO_PI)))
                      c (* 2 (Math/cos (mod (+ (* 0.001 millis) (last tuple)) PApplet/TWO_PI)))
                      loud (+ 50 (* 1 (first (get sounds "/0/loudness"))))
                      ]

                  (doall (map (fn [y]
                                (.pushMatrix pG)

                                (if (= 0 y)
                                  ;(.fill pG 170 180 50 200)
                                  (.fill pG (* 2 (first (get cc "/cc/85")))
                                         (* 2 (first (get cc "/cc/104")))
                                         (* 2 (first (get cc "/cc/112")))
                                         200)
                                  (.fill pG (mod (+ m (* 32 y)) 255) 200 200 (+ 30 (* 1000 (last sines))))
                                  )


                                (.translate pG (* 0.03 (first sines)) (+ c (* 6 y)) s)
                                (let [
                                      l (int (*
                                               (* 10 (first (get cc "/cc/75")))
                                               (first (get sounds "/0/noisiness"))
                                               (first (get sounds "/0/noisiness"))))
                                      sh (int (* 2 (first (get cc "/cc/86"))))
                                      ss (int (* 2 (first (get cc "/cc/105"))))
                                      sb (int (* 2 (first (get cc "/cc/113"))))
                                      s (int (* 2 (first (get cc "/cc/76"))))
                                      rx (- (rand (* 2 l)) l)
                                      ry (- (rand (* 2 l)) l)
                                      rz (- (rand (* 2 l)) l)
                                      ]
                                  (.stroke pG (+ sh (- (rand 90) 45)) ss sb (rand 100))
                                  (.strokeWeight pG (rand (* 0.1 s)))
                                  (.line pG 0 0 0 rx ry rz)
                                  )
                                (.stroke pG 140 100 255 (+ 1 (* 150 (last sines))))


                                (.scale pG 10 1 10)
                                (.sphereDetail pG 4)
                                (.sphere pG 0.1)
                                (.popMatrix pG)

                                ) (range 0 (* 100 (last sines)) 0.05)))
                  )
                (.popMatrix pG)
                )
              (map (fn [i] [(str "/0/sines/" i) i]) (range 1 21))))

  )

