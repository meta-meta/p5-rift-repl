(ns music_staff
  (:import (processing.core PApplet)))

(import processing.core.PApplet)


(defn drawme [this pG sounds]
  (.background pG 255 50 100)

  (.stroke pG 255)
  (.strokeWeight pG 3)


  (.scale pG 0.01 0.01)
  (.strokeWeight pG 30)

  (defn dStaffSeg [x z1 z2] (doall (map
                           (fn [stave]
                             (.line pG
                                    0 stave z1
                                    (+ 1 0) stave z2)
                             )
                           (range 0 6)
                           )))

  (dStaffSeg 0 1 1.1)
  (dStaffSeg 1 1 1.1)
  (dStaffSeg 2 1 1.1)

  (doall (map
           dStaffSeg
           (range 0 3) (range 0 1 0.1) (range 0.1 1 0.01)))

  )

