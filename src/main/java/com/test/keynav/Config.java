package com.test.keynav;

import java.io.IOException;
import java.util.Properties;

public class Config {
    private static Properties p = new Properties();

    private static final String file = "conf.properties";

    static {
        try {
            p.load(Config.class.getClassLoader().getResourceAsStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static boolean equal(String key, int vkCode) {
        String val = p.getProperty(key);
        return val.equals(String.valueOf(vkCode));
    }

    public static int getInt(String key) {
        return Integer.parseInt(p.getProperty(key));
    }

    public static float getFloat(String key) {
        return Float.parseFloat(p.getProperty(key));
    }
}
