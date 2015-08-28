(ns music_staff
  (:import (processing.core PApplet))
  (:use p5-help)
  )

(import processing.core.PApplet)
(import com.generalprocessingunit.processing.music.MusicalFontConstants)

(def p5 (atom nil))


(defrecord Measure [beats-per-measure length-of-beat phrases])
(defrecord Phrase [events])
(defrecord Event [type beats notes])


(def measures (Measure. 4 1/4 [(Phrase. [
                                         (Event. :note 1/2 [1 2 3 4])
                                         (Event. :note 1/2 [2 4 5])
                                         ])
                               (Phrase. [
                                         (Event. :note 1 [0])
                                         (Event. :rest 1 [])
                                         ])
                               ]))


(defn glyph-note [beats]
  (cond
    (= beats 1)
    MusicalFontConstants/NOTE_WHOLE
    (= beats 1/2)
    MusicalFontConstants/NOTE_HALF_UP
    (= beats 1/4)
    MusicalFontConstants/NOTE_QUARTER_UP
    (= beats 1/8)
    MusicalFontConstants/NOTE_EIGHTH_UP
    (= beats 1/16)
    MusicalFontConstants/NOTE_16TH_UP
    (= beats 1/32)
    MusicalFontConstants/NOTE_32ND_UP
    )
  )

(defn glyph-rest [beats]
  (cond
    (= beats 1)
    MusicalFontConstants/REST_WHOLE
    (= beats 1/2)
    MusicalFontConstants/REST_HALF
    (= beats 1/4)
    MusicalFontConstants/REST_QUARTER
    (= beats 1/8)
    MusicalFontConstants/REST_EIGTH
    (= beats 1/16)
    MusicalFontConstants/REST_16TH
    (= beats 1/32)
    MusicalFontConstants/REST_32ND
    )
  )


; Measure Queue
(def queue (atom (clojure.lang.PersistentQueue/EMPTY)))
(def millis-at-play (atom 0))

(defn add-measure [measure]
  (if (nil? (peek @queue))
    (swap! millis-at-play (fn [x] (.millis @p5))))
  (swap! queue conj measure))

(defn next-measure []
  (let [ret (peek @queue)]
    (swap! queue pop)
    ret
    )
  )


(defn setup [this]
  (swap! p5 (constantly this))

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
        segments (range 0 50)
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

    (defn draw-measure [measure]
      (doall (map
               (fn [phrase]
                 (reduce
                   (fn [curr-beat event]
                     (cond
                       (= :note (:type event))
                       (doall (map
                                (fn [n] (d-event
                                          (+ 10 curr-beat)
                                          n
                                          (glyph-note (:beats event))))
                                (:notes event)))

                       (= :rest (:type event))
                       (d-event
                         (+ 10 curr-beat)
                         2
                         (glyph-rest (:beats event)))
                       )

                     (+ curr-beat (/ (:beats event) (:length-of-beat measure)))
                     )
                   0
                   (:events phrase))
                 )
               (:phrases measure)))
      )

    (draw-measure measures)


    (.hint pG PApplet/ENABLE_DEPTH_MASK)
    )




  )

