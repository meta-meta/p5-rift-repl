(ns p5-rift-core)

(gen-class
  :name p5-rift-core.RiftReplClj
  :extends com.generalprocessingunit.processing.vr.RiftRepl
  :prefix "p5-")

(import com.generalprocessingunit.processing.vr.PAppletVR)

(defn p5-drawReplView [this eye pG]
  (.background pG 10 10 (* 20 (Math/sin (/ (.millis this) 600))))

  (.stroke pG 255)
  (.pushMatrix pG)

  (.translate pG (* 1 (Math/cos (* 0.001 (.millis this)))) 1 1)
  (.sphere pG 0.2)

  (.fill pG 255 0 0)
  (.translate pG (* 1 (Math/cos (* 0.003 (.millis this)))) 1 1)
  (.sphere pG 0.2)
  (.popMatrix pG)

  )

(defn start []
  (PAppletVR/main "p5-rift-core.RiftReplClj"))
