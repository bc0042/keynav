package com.test.keynav;

import javax.swing.*;
import java.awt.*;

public class test {
    public static void main(String[] args) {
        JFrame jFrame = new JFrame();
        jFrame.setVisible(true);
        jFrame.setSize(3000,1080);
    }
    public static void main3(String[] args) {
        GraphicsDevice graphicsDevice = MyDevice.curr();
        System.out.println(graphicsDevice.getType());
        System.out.println();
    }
    public static void main2(String[] args) throws AWTException {
        GraphicsDevice d = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        System.out.println(d.getDisplayMode().getWidth());
        System.out.println(d.getDisplayMode().getHeight());
        System.out.println();
//        GraphicsDevice device = MouseInfo.getPointerInfo().getDevice();
//        System.out.println(device);
        Robot robot = new Robot(d);
        robot.mouseMove(2000,99);
    }
    public static void main1(String[] args) {
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        GraphicsConfiguration conf = devices[0].getDefaultConfiguration();
        GraphicsConfiguration conf1 = devices[1].getDefaultConfiguration();
        JFrame jFrame = new JFrame(conf);
        JFrame jFrame2 = new JFrame(conf1);
        jFrame.setVisible(true);
        jFrame2.setVisible(true);
    }
}
