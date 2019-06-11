package com.test.keynav;

import java.awt.*;
import java.awt.event.InputEvent;

public class MyRobot {
    public static Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static void mouseMove(int x, int y) {
        robot.mouseMove(x, y);
    }


    public static void leftClick(int delay) {
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(delay); // bug fixed for some stupid software
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }

    public static void rightClick(int delay) {
        robot.mousePress(InputEvent.BUTTON3_MASK);
        robot.delay(delay); // bug fixed for some stupid software
        robot.mouseRelease(InputEvent.BUTTON3_MASK);
    }

    public static void mouseWheel(int wheelAmt) {
        robot.mouseWheel(wheelAmt);
    }

    public static Point currPoint() {
        return MouseInfo.getPointerInfo().getLocation();
    }

    public static void move2Start() {
        int x = MyDevice.width / 2;
        int y = MyDevice.height / 2;
        mouseMove(x, y);
    }

}
