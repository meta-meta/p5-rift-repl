package com.generalprocessingunit.processing;

import com.generalprocessingunit.hid.SpaceNavigator;
import processing.core.PGraphics;

public class P5Repl extends PAppletBuffered {
    SpaceNavigator spaceNav;

    @Override
    public void setup() {
        super.setup();
        spaceNav = new SpaceNavigator(this);
    }

    boolean err;

    @Override
    public void draw(PGraphics pG) {
        spaceNav.poll();

        try{
            drawReplView(pG, spaceNav);
            err = false;
        } catch (Exception e) {
            if(!err) {
                drawErrorScreen(pG);
                println("Error drawing ~~~~~~~~~~~~~~");
                e.printStackTrace();
                err = true;
            }
        }
    }

    public void drawReplView(PGraphics pG, SpaceNavigator spaceNav) {}

    private void drawErrorScreen(PGraphics pG) {
        pG.background(200, 50, 0);
    }
}
