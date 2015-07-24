package com.generalprocessingunit.processing;

import com.generalprocessingunit.hid.SpaceNavMomentum;
import com.generalprocessingunit.hid.SpaceNavigator;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class P5ReplDualMon extends PApplet {
    SpaceNavMomentum spaceNav;

    PGraphics[] pGs = new PGraphics[2];
    private PFrame mon2;

    Map<Integer, Boolean> keys = new HashMap<>();
    private int[] scroll = new int[2];

    @Override
    public void setup() {
        //TODO: this forces the two monitors to be same res. flexibility would be nice
        for (int i = 0; i < 2; i++) {
            pGs[i] = createGraphics(width, height, P3D);
        }

        mon2 = new PFrame(width, height);

        spaceNav = new SpaceNavMomentum(this);
        noCursor();

        scroll[0] = 100;
        scroll[1] = 100;
    }

    @Override
    public final void draw() {
        spaceNav.poll();

        for (int i = 0; i < 2; i++) {
            pGs[i].beginDraw();

            try{
                drawReplView(pGs[i], i, spaceNav, keys, scroll[i]);
                err = false;
            } catch (Exception e) {
                if(!err) {
                    drawErrorScreen(pGs[i]);
                    println("Error drawing ~~~~~~~~~~~~~~");
                    e.printStackTrace();
                    err = true;
                }
            }

            pGs[i].endDraw();
        }


        image(pGs[0], 0, 0);

        applet2.image(pGs[1].get(), 0, 0, applet2.w, applet2.h);
        applet2.redraw();
    }

    boolean err;

    private SecondApplet applet2;
    private static boolean DRAW_TO_SECOND_WINDOW = false;
    private class PFrame extends Frame {
        int w, h;
        public PFrame(int w, int h) {
            this.w = w;
            this.h = h;
            setBounds(2970, 0, w, h); //TODO: this should come from an arg
            setUndecorated(true);
            setAlwaysOnTop(true);
            applet2 = new SecondApplet(w, h);
            add(applet2);
            applet2.init();
            setVisible(true);
        }
    }

    private class SecondApplet extends PApplet {
        int w, h;
        public SecondApplet(int w, int h) {
            this.w = w;
            this.h = h;
        }
        public void setup() {
            size(w, h);
            noLoop();
            noCursor();
        }

        public void draw() {}

        @Override
        public void mouseWheel(MouseEvent event) {
            super.mouseWheel(event);
            scroll[1] -= event.getCount();
        }

        @Override
        public void keyPressed(KeyEvent e)
        {
            // reset scroll on F
            if(e.getKeyCode() == 70) {
                scroll[1] = 100;
            }

            keys.put(e.getKeyCode(), true);
        }

        @Override
        public void keyReleased(KeyEvent e)
        {
            keys.put(e.getKeyCode(), false);
        }
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        super.mouseWheel(event);
        scroll[0] -= event.getCount();
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        // reset scroll on F
        if(e.getKeyCode() == 70) {
            scroll[0] = 100;
        }

        keys.put(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        keys.put(e.getKeyCode(), false);
    }

    public void drawReplView(PGraphics pG, int mon, SpaceNavigator spaceNav, Map<Integer, Boolean> keys, int scroll) {}

    private void drawErrorScreen(PGraphics pG) {
        pG.background(200, 50, 0);
    }
}
