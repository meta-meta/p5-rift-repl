(ns p5-help
  (:import (processing.core PApplet)))

(defmacro push-pop [pG & body]
  (list 'do '(.pushMatrix pG) (cons 'do body) '(.popMatrix pG))
  )
