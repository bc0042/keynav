package com.test.keynav;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;
import com.sun.jna.platform.win32.WinUser.MSG;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Sample implementation of a low-level keyboard hook on W32.
 */
public class Keynav {
    private static boolean ctrlDown = false;
    private static boolean scrollMode = false;
    private static HHOOK hhk;
    private static Robot robot;
    private static MyPanel myPanel;
    private static JFrame jFrame = new JFrame();
    private static List<Point> savePoints = new ArrayList<>();
    private static LinkedList<Point> history = new LinkedList<>();
    private static int scale;

    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String UP = "up";
    private static final String DOWN = "down";
    private static final String UPPER_LEFT = "upperLeft";
    private static final String UPPER_RIGHT = "upperRight";
    private static final String LOWER_LEFT = "lowerLeft";
    private static final String LOWER_RIGHT = "lowerRight";


    public static void main(String[] args) throws AWTException {
        scale = Config.getInt("scale");
        myPanel = new MyPanel();
        jFrame.add(myPanel);
        jFrame.setUndecorated(true);
        jFrame.setOpacity(0.2f);
        jFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        robot = new Robot();
        final User32 lib = User32.INSTANCE;
        HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
        LowLevelKeyboardProc keyboardHook = (nCode, wParam, info) -> {
            if (nCode >= 0) {
                switch (wParam.intValue()) {
                    case WinUser.WM_KEYDOWN:
                        keyDown(info.vkCode);
                        break;
                    case WinUser.WM_KEYUP:
                        keyUp(info.vkCode);
                        break;
                }
            }
            Pointer ptr = info.getPointer();
            long peer = Pointer.nativeValue(ptr);
            return lib.CallNextHookEx(hhk, nCode, wParam, new LPARAM(peer));
        };
        hhk = lib.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, keyboardHook, hMod, 0);

        System.out.println("start..");
        // This bit never returns from GetMessage
        int result;
        MSG msg = new MSG();
        while ((result = lib.GetMessage(msg, null, 0, 0)) != 0) {
            if (result == -1) {
                System.err.println("error in get message");
                break;
            } else {
//                System.err.println("got message");
                lib.TranslateMessage(msg);
                lib.DispatchMessage(msg);
            }
        }
        lib.UnhookWindowsHookEx(hhk);
    }

    private static void keyUp(int vkCode) {
        if (Config.equal("ctrl", vkCode)) {
            ctrlDown = false;
        }
    }

    private static void keyDown(int vkCode) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Point p = MouseInfo.getPointerInfo().getLocation();
        boolean keyMode = jFrame.isVisible();
        System.out.println("key code: " + vkCode);

        if (Config.equal("ctrl", vkCode)) {
            ctrlDown = true;
            return;
        }
        if (ctrlDown && Config.equal("begin", vkCode)) { //begin
            scale = 1;
            robot.mouseMove(screenSize.width / 2, screenSize.height / 2);
            myPanel.paint(scale);
            jFrame.setVisible(true);
            System.out.println("key mode on..");
            return;
        }
        if (ctrlDown && Config.equal("begin2", vkCode)) { //begin2
            scale = 8;
            myPanel.paint(scale);
            jFrame.setVisible(true);
            System.out.println("key mode on.. scale=16");
            return;
        }
        if (Config.equal("esc", vkCode)) { //escape
            jFrame.setVisible(false);
            scrollMode = false;
            System.out.println("key mode off..");
            System.out.println("scroll mode off..");
            return;
        }

        if (keyMode && Config.equal("enter", vkCode)) { //left click
            jFrame.setVisible(false);
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            System.out.println("key mode off..");
            return;
        }
        if (keyMode && Config.equal("space", vkCode)) { //right click
            jFrame.setVisible(false);
            robot.mousePress(InputEvent.BUTTON3_MASK);
            robot.mouseRelease(InputEvent.BUTTON3_MASK);
            return;
        }
        if (keyMode && ctrlDown && Config.equal("key1", vkCode)) { // save1
            savePoints.add(p);
            System.out.println("key1..");
            return;
        }
        if (keyMode && ctrlDown && Config.equal("key2", vkCode)) { // save2
            savePoints.add(p);
            System.out.println("key2..");
            return;
        }
        if (keyMode && ctrlDown && Config.equal("key3", vkCode)) { // save3
            savePoints.add(p);
            System.out.println("key3..");
            return;
        }
        if (keyMode && Config.equal("key1", vkCode)) { // restore1
            if (savePoints.size() >= 1) {
                Point point = savePoints.get(0);
                robot.mouseMove(point.x, point.y);
            }
            return;
        }
        if (keyMode && Config.equal("key2", vkCode)) { // restore2
            if (savePoints.size() >= 2) {
                Point point = savePoints.get(1);
                robot.mouseMove(point.x, point.y);
            }
            return;
        }
        if (keyMode && Config.equal("key3", vkCode)) { // restore3
            if (savePoints.size() >= 3) {
                Point point = savePoints.get(2);
                robot.mouseMove(point.x, point.y);
            }
            return;
        }

        if (keyMode && Config.equal(UPPER_LEFT, vkCode)) { // move upperLeft
            move(UPPER_LEFT);
            return;
        }
        if (keyMode && Config.equal(UPPER_RIGHT, vkCode)) { // move upperRight
            move(UPPER_RIGHT);
            return;
        }
        if (keyMode && Config.equal(LOWER_LEFT, vkCode)) { // move lowerLeft
            move(LOWER_LEFT);
            return;
        }
        if (keyMode && Config.equal(LOWER_RIGHT, vkCode)) { // move lowerRight
            move(LOWER_RIGHT);
            return;
        }

        if (keyMode && Config.equal(LEFT, vkCode)) { // move left
            move(LEFT);
            return;
        }
        if (keyMode && Config.equal(DOWN, vkCode)) { // move down
            move(DOWN);
            return;
        }
        if (keyMode && Config.equal(UP, vkCode)) { // move up
            move(UP);
            return;
        }
        if (keyMode && Config.equal(RIGHT, vkCode)) { // move right
            move(RIGHT);
            return;
        }

        if (keyMode && ctrlDown && Config.equal("back", vkCode)) { // move back
            Point last = history.removeLast();
            history.addFirst(last);
            robot.mouseMove(last.x, last.y);
            return;
        }
        if (keyMode && ctrlDown && Config.equal("forward", vkCode)) { // move forward
            Point first = history.removeFirst();
            history.addLast(first);
            robot.mouseMove(first.x, first.y);
            return;
        }

        if (keyMode && Config.equal("scrollDown", vkCode)) { // scroll on
            scrollMode = true;
            jFrame.setVisible(false);
            System.out.println("scroll on..");
            return;
        }
        if (scrollMode && Config.equal("scrollDown", vkCode)) { // scroll down
            robot.mouseWheel(Config.getInt("wheelAmt"));
            return;
        }
        if (scrollMode && Config.equal("scrollUp", vkCode)) { // scroll up
            robot.mouseWheel(-Config.getInt("wheelAmt"));
            return;
        }

    }

    private static void move(String flag) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Point p = MouseInfo.getPointerInfo().getLocation();
        if (!ctrlDown) {
            scale *= 2;
        }
        int x = 0, y = 0;
        int offsetX = (int) (screenSize.getWidth() / scale / 2);
        int offsetY = (int) (screenSize.getHeight() / scale / 2);
        if (UPPER_LEFT.equals(flag)) {
            x = p.x - offsetX;
            y = p.y - offsetY;
        } else if (UPPER_RIGHT.equals(flag)) {
            x = p.x + offsetX;
            y = p.y - offsetY;
        } else if (LOWER_LEFT.equals(flag)) {
            x = p.x - offsetX;
            y = p.y + offsetY;
        } else if (LOWER_RIGHT.equals(flag)) {
            x = p.x + offsetX;
            y = p.y + offsetY;
        } else if (UP.equals(flag)) {
            x = p.x;
            y = p.y - offsetY;
        } else if (DOWN.equals(flag)) {
            x = p.x;
            y = p.y + offsetY;
        } else if (LEFT.equals(flag)) {
            x = p.x - offsetX;
            y = p.y;
        } else if (RIGHT.equals(flag)) {
            x = p.x + offsetX;
            y = p.y;
        }
        robot.mouseMove(x, y);
        myPanel.paint(scale);
        addHistory();
    }

    private static void addHistory() {
        Point p = MouseInfo.getPointerInfo().getLocation();
        if (history.size() >= Config.getInt("historyMax")) {
            history.removeFirst();
        }
        history.add(p);
    }

}
