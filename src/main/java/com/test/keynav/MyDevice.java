package com.test.keynav;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MyDevice {
    public static int width;
    public static int height;
    public static GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    private static Map<String, GraphicsDevice> map = new HashMap<>();

    public static GraphicsDevice curr() {
        return MouseInfo.getPointerInfo().getDevice();
    }

    public static void init() {
        for (GraphicsDevice device : devices) {
            DisplayMode displayMode = device.getDisplayMode();
            System.out.println(device.getIDstring() + "  " + displayMode.getWidth() + "x" + displayMode.getHeight());
            width += displayMode.getWidth();
            height = displayMode.getHeight() > height ? displayMode.getHeight() : height;
            map.put(device.getIDstring(), device);
        }

        System.out.println(String.format("resolution: %s x %s", width, height));
    }
}
