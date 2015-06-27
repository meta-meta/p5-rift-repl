package com.generalprocessingunit.processing.vr;

import processing.core.PGraphics;
import processing.core.PImage;

import java.awt.*;


public class RiftRepl extends PAppletVR {
    Robot robot;

    Rectangle rectangle;
    PImage screenshot;

    class Screencap implements Runnable {
        @Override
        public void run() {
            while(true) {
                robot.createScreenCapture(rectangle).getRGB(0, 0, screenshot.width, screenshot.height, screenshot.pixels, 0, screenshot.width);
                screenshot.updatePixels();
            }
        }
    }

    public static void main(String[] args){
        PAppletVR.main(RiftRepl.class);
    }


    @Override
    public void setup() {
        super.setup();

        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        GraphicsDevice[] g = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        rectangle = g[1].getConfigurations()[0].getBounds();
//        rectangle.setSize(500, 500);
        screenshot = new PImage(rectangle.width, rectangle.height, ARGB);

        new Thread(new Screencap()).start();

        headContainer.setLocation(0, 2, -1);
    }

    @Override
    protected void updateState() {
    }


    boolean err;

    @Override
    protected void drawView(int eye, PGraphics pG) {
        try{
            drawReplView(eye, pG);
            err = false;
        } catch (Exception e) {
            if(!err) {
                e.printStackTrace();
                err = true;
            }
        }

        pushMatrix();
        {
            pG.translate(0, 1f, 300f);
            pG.scale(1, -1, 1);
            pG.rotateX(-.2f);
            screen(pG, screenshot, .1f * screenshot.width, .1f * screenshot.height);
        }
        popMatrix();
    }

    public void drawReplView(int eye, PGraphics pG) {}

    void screen(PGraphics pG, PImage tex, float halfW, float halfH) {
//        pG.colorMode(RGB);
//        pG.fill(255,255,0);
//        pG.rect(-halfW, -halfH, halfW * 2, halfH * 2);

        pG.blendMode(LIGHTEST);
        pG.beginShape(QUADS);
        pG.textureMode(NORMAL);
        pG.texture(tex);
        pG.emissive(255);

        pG.vertex(-halfW, -halfH, 0, 0, 0);
        pG.vertex(halfW, -halfH, 0, 1, 0);
        pG.vertex(halfW, halfH, 0, 1, 1);
        pG.vertex(-halfW, halfH, 0, 0, 1);

        pG.endShape();
        pG.blendMode(BLEND);
    }
}
