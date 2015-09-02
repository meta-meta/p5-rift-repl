(ns music_staff
  (:import (processing.core PApplet))
  (:use p5-help)
  )

(import processing.core.PApplet)
(import com.generalprocessingunit.processing.music.MusicalFontConstants)

(def p5 (atom nil))


(def names {
            :chromatic "Chromatic"
            :diatonic "Diatonic"
            :pentatonic "Pentatonic"
            :whole-tone "Whole Tone"
            :octatonic "Octatonic"
            })

;; NoteSequence is a vector of MIDI note numbers
(defrecord NoteSequence [name seq])

;; IntervalSequence is a vector of intervals
(defrecord IntervalSequence [name seq])

;; Scale is an ascending interval sequence guaranteed to span exactly an octave
(defrecord Scale [name seq])
(with-meta ->Scale {:doc "howdy"})

;; Key is a set of pitch-classes and a tonic
(defrecord Key [name pitch-classes tonic])

;; todo: add this to protocol covering intervalSeq and scales
(defn scale-to-note-seq [scale starting-note]
  (->> (range (count (:seq scale)))
       (map #(reduce + starting-note (take % (:seq scale))))))

(defn key-from-scale [scale tonic]
  (Key.
    (:name scale)
    (->> (scale-to-note-seq scale tonic)
         (map #(mod % 12)))
    tonic
    )
  )


(def all-notes (set (range 0 128)))
(def ewi-notes (set (range 34 100)))

(def scales {
             :chromatic (Scale. :chromatic (repeat 12 1))
             :diatonic (Scale. :diatonic [2 2 1 2 2 2 1])
             :pentatonic (Scale. :pentatonic [3 2 3 2 2])
             :whole-tone (Scale. :whole-tone [2 2 2 2 2 2])
             :octatonic (Scale. :octatonic [1 2 1 2 1 2 1 2])
             })

(def pitch-classes [:c :db :d :e :eb :f :f# :g :ab :a :b])

(def keys (zipmap
            pitch-classes
            (map
              (partial key-from-scale
                       (:diatonic scales))
              (range))))

(def mode-names-major-scale-order [:ionian :dorian :phrygian :lydian :mixolydian :aeolian :locrian])
(def mode-names-bright-to-dark-order [:lydian :ionian :mixolydian :dorian :aeolian :phrygian :locrian])

(def modes (let [names mode-names-major-scale-order]
             (zipmap
               names
               (map (fn [i name]
                      (let [s (:seq (:diatonic scales))]
                        (Scale. name
                                (flatten (cons
                                           (take-last (- (count s) i) s)
                                           (take i s))))
                        ))
                    (range) names))))

(def solfege-chromatic [:oh :one :two :three :four :five :six :sev :eight :nine :ten :el])

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
  (case beats
    1 MusicalFontConstants/NOTE_WHOLE
    1/2 MusicalFontConstants/NOTE_HALF_UP
    1/4 MusicalFontConstants/NOTE_QUARTER_UP
    1/8 MusicalFontConstants/NOTE_EIGHTH_UP
    1/16 MusicalFontConstants/NOTE_16TH_UP
    1/32 MusicalFontConstants/NOTE_32ND_UP
    )
  )

(defn glyph-rest [beats]
  (case beats
    1 MusicalFontConstants/REST_WHOLE
    1/2 MusicalFontConstants/REST_HALF
    1/4 MusicalFontConstants/REST_QUARTER
    1/8 MusicalFontConstants/REST_EIGTH
    1/16 MusicalFontConstants/REST_16TH
    1/32 MusicalFontConstants/REST_32ND
    )
  )

(def note-letters [:a :b :c :d :e :f :g])

(defrecord Clef [glyph glyph-position lowest-note])
(def g-clef (Clef. MusicalFontConstants/G_CLEF 2 64))       ;E4
(def f-clef (Clef. MusicalFontConstants/F_CLEF 6 43))       ;G2

; translate from midi number to position on clef
(defn midi-to-clef-position [note clef]
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

  (defn d-glyph [glyph]
    (push-pop pG
              (.textFont pG bravura 4.8)
              (.scale pG 1 -1)
              (.text pG glyph (float 0) (float 0))
              ))

  (def seg-scale 2)

  (defn d-staff-seg [seg z1 z2 alpha]
    (defn d-stave-seg [staff-position]
      (.stroke pG 0 0 0 (int alpha))
      (.line pG
             (* seg-scale seg)
             staff-position
             z1

             (* seg-scale (+ 1 seg))
             staff-position
             z2)
      )

    ; draw the 5 staff lines
    (doall (map
             d-stave-seg
             (range 0 10 2)
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
                              (* seg-scale seg)
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

