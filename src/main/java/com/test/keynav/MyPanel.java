package com.test.keynav;

import javax.swing.*;
import java.awt.*;

public class MyPanel extends JPanel {
    static int x = 0;
    static int y = 0;
    static int w = 0;
    static int h = 0;

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.red);
        g.drawRect(x, y, w, h);
        g.drawLine(x, y + h / 2, x + w, y + h / 2);
        g.drawLine(x + w / 2, y, x + w / 2, y + h);
    }

    public void paint(int scale) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Point p = MouseInfo.getPointerInfo().getLocation();
        int scaleX = screenSize.width / scale;
        int scaleY = screenSize.height / scale;
        this.x = p.x - scaleX / 2;
        this.y = p.y - scaleY / 2;
        this.w = scaleX;
        this.h = scaleY;
        repaint();
    }
}
