package com.generalprocessingunit.processing;

import processing.core.PGraphics;

public class P5Repl extends PAppletBuffered {

    @Override
    public void setup() {
        super.setup();
    }

    boolean err;

    @Override
    public void draw(PGraphics pG) {
        try{
            drawReplView(pG);
            err = false;
        } catch (Exception e) {
            if(!err) {
                drawErrorScreen(pG);
                e.printStackTrace();
                err = true;
            }
        }
    }

    public void drawReplView(PGraphics pG) {}

    private void drawErrorScreen(PGraphics pG) {
        pG.background(200, 50, 0);
    }
}
