(ns music_staff
  (:import (processing.core PApplet))
  (:use p5-help)
  )

(import processing.core.PApplet)
(import com.generalprocessingunit.processing.music.MusicalFontConstants)

(def p5 (ref nil))


(defrecord Event [type duration])



(defn setup [this]
  (dosync (ref-set p5 this))

  ;TODO Bravura.otf needs to be copied to the classpath  build/classes/main works
  (def bravura (.createFont this "Bravura.otf" 100 true MusicalFontConstants/charset))
  )


(defn drawme [this pG sounds]



  (.background pG 60 50 100)

  (.stroke pG 0)
  (.strokeWeight pG 3)

  ;(.scale pG 0.01 0.01)

  (defn d-glyph [glyph]
    (push-pop pG
              (.textFont pG bravura 2.5)
              (.scale pG 1 -1)
              (.text pG glyph (float 0) (float 0))
              ))

  (defn d-staff-seg [seg z1 z2 alpha]
    (defn d-stave-seg [staff-position]
      (.stroke pG 0 0 0 (int alpha))
      (.line pG
             seg staff-position z1
             (+ 1 seg) staff-position z2)
      )

    ; draw the 5 staff lines
    (doall (map
             d-stave-seg
             (range 0 5)
             ))

    )



  ; draw everything
  (let [
        depth 25
        staff-z-fn (fn [x]
                        (* depth (Math/sin (+ -0.5 PApplet/PI PApplet/HALF_PI (* 0.1 x)))))
        segments (range 0 60)
        alphas (range 255 0 -5)
        ]


    (defn d-event [seg staff-position glyph]
      (push-pop pG
                (let [alpha (nth alphas seg)]
                  (.translate pG
                              seg
                              staff-position
                              (staff-z-fn seg))
                  (.fill pG 0 0 0 alpha))
                (d-glyph glyph)
                )
      )


    (defn d-staff []
      (defn staff-z [start-at]
        (map staff-z-fn
             (range start-at Integer/MAX_VALUE)))

      (doall (map
               d-staff-seg
               segments
               (staff-z 0)
               (staff-z 1)
               alphas))
      )
    (d-staff)

    (.hint pG PApplet/DISABLE_DEPTH_MASK)

    (d-event 5.5 3 MusicalFontConstants/FLAT)
    (d-event 6 1 MusicalFontConstants/NOTE_QUARTER_UP)
    (d-event 6 2 MusicalFontConstants/NOTE_QUARTER_UP)
    (d-event 6 3 MusicalFontConstants/NOTE_QUARTER_UP)
    (d-event 20 0 MusicalFontConstants/SHARP)


    (.hint pG PApplet/ENABLE_DEPTH_MASK)
    )




  )

