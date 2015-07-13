package com.generalprocessingunit.processing;

import com.generalprocessingunit.hid.SpaceNavigator;
import processing.core.PGraphics;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class P5Repl extends PAppletBuffered {
    SpaceNavigator spaceNav;

    Map<Integer, Boolean> keys = new HashMap<>();

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
            drawReplView(pG, spaceNav, keys);
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

    @Override
    public void keyPressed(KeyEvent e)
    {
        keys.put(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        keys.put(e.getKeyCode(), false);
    }

    public void drawReplView(PGraphics pG, SpaceNavigator spaceNav, Map<Integer, Boolean> keys) {}

    private void drawErrorScreen(PGraphics pG) {
        pG.background(200, 50, 0);
    }
}
