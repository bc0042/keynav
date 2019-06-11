package com.test.keynav;

import javax.swing.*;

public class MyFrame {
    private static JFrame jFrame = new JFrame();

    static {
        jFrame.setContentPane(new MyPanel());
        jFrame.setUndecorated(true);
        jFrame.setOpacity(Config.getFloat("opacity"));
        jFrame.setSize(MyDevice.width, MyDevice.height);
    }

    public static void showAndPaint(int scale, boolean beginning) {
        jFrame.setVisible(true);
        paint(scale, beginning);
    }

    public static boolean isVisible() {
        return jFrame.isVisible();
    }

    public static void setVisible(boolean b) {
        jFrame.setVisible(b);
    }

    public static void paint(int scale, boolean beginning) {
        ((MyPanel) jFrame.getContentPane()).paint(scale, beginning);
    }
}
