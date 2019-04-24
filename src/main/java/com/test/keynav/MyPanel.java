package com.test.keynav;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MyPanel extends JPanel {
    private static int x = 0;
    private static int y = 0;
    private static int w = 0;
    private static int h = 0;
    private static boolean beginning;
    static Map locations = new HashMap();

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.red);
        g.drawRect(x, y, w, h);

        if (beginning) {
            int blockSize = Config.getInt("blockSize");
            int w2 = w / blockSize;
            int h2 = h / blockSize;
            for (int i = w2; i < w; i += w2) {
                g.drawLine(i, 0, i, h);
            }
            for (int j = h2; j < h; j += h2) {
                g.drawLine(0, j, w, j);
            }
            int startNum = 10;
            g.setFont(new Font(null, 0, blockSize * 3));
            for (int i = 0; i < blockSize; i++) {
                for (int j = 0; j < blockSize; j++) {
                    int n = startNum + j * blockSize + i;
                    String str = n + "";
                    int x1 = w / blockSize / 2 + i * w2;
                    int y1 = h / blockSize / 2 + j * h2;
                    g.drawString(str, x1, y1);
                    locations.put(str, new Point(x1, y1));
                }
            }
        } else {
            g.drawLine(x, y + h / 2, x + w, y + h / 2);
            g.drawLine(x + w / 2, y, x + w / 2, y + h);
        }
    }

    public void paint(int scale, boolean beginning) {
        this.beginning = beginning;
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
