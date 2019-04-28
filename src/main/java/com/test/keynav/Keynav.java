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

/**
 * Sample implementation of a low-level keyboard hook on W32.
 */
public class Keynav {
    private static boolean ctrlDown = false;
    private static boolean scrollMode = false;
    private static boolean moveMode = false;
    private static HHOOK hhk;
    private static Robot robot;
    private static MyPanel myPanel;
    private static JFrame jFrame = new JFrame();
    private static Point[] savePoints = new Point[3];
    private static MyQueue history;
    private static MyQueue input;
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
        input = new MyQueue(2);
        history = new MyQueue(Config.getInt("historySize"));
        myPanel = new MyPanel();
        jFrame.add(myPanel);
        jFrame.setUndecorated(true);
        jFrame.setOpacity(Config.getFloat("opacity"));
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
            myPanel.paint(scale, true);
            jFrame.setVisible(true);
            input.clear();
            System.out.println("key mode on..");
            return;
        }
        if (ctrlDown && Config.equal("begin2", vkCode)) { //begin2
            scale = 8;
            myPanel.paint(scale, false);
            jFrame.setVisible(true);
            System.out.println("key mode on.. scale=" + scale);
            return;
        }
        if (Config.equal("esc", vkCode)) { //escape
            jFrame.setVisible(false);
            scrollMode = false;
            moveMode = false;
            input.clear();
            System.out.println("escape..");
            return;
        }

        if (keyMode && vkCode >= Config.getInt("key0") && vkCode <= Config.getInt("key9")) { // input numbers
            input.add(vkCode - Config.getInt("key0"));
            if (input.size() == 2) {
                Point restore = (Point) MyPanel.locations.get(input.getFirst() + "" + input.getLast());
                robot.mouseMove(restore.x, restore.y);
                input.clear();
                scale = 8;
                myPanel.paint(scale, false);
            }
            return;
        }

        if ((keyMode || moveMode) && Config.equal("key_e", vkCode)) { //left click with delay
            jFrame.setVisible(false);
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.delay(10); // bug fixed for some stupid software
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
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
            savePoints[0] = p;
            System.out.println("key1..");
            return;
        }
        if (keyMode && ctrlDown && Config.equal("key2", vkCode)) { // save2
            savePoints[1] = p;
            System.out.println("key2..");
            return;
        }
        if (keyMode && ctrlDown && Config.equal("key3", vkCode)) { // save3
            savePoints[2] = p;
            System.out.println("key3..");
            return;
        }
        if (keyMode && Config.equal("key1", vkCode)) { // restore1
            Point point = savePoints[0];
            if (point != null) {
                robot.mouseMove(point.x, point.y);
            }
            return;
        }
        if (keyMode && Config.equal("key2", vkCode)) { // restore2
            Point point = savePoints[0];
            if (point != null) {
                robot.mouseMove(point.x, point.y);
            }
            return;
        }
        if (keyMode && Config.equal("key3", vkCode)) { // restore3
            Point point = savePoints[0];
            if (point != null) {
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
            Point last = (Point) history.removeLast();
            history.addFirst(last);
            robot.mouseMove(last.x, last.y);
            return;
        }
        if (keyMode && ctrlDown && Config.equal("forward", vkCode)) { // move forward
            Point first = (Point) history.removeFirst();
            history.addLast(first);
            robot.mouseMove(first.x, first.y);
            return;
        }

        if (keyMode && Config.equal("scrollDown", vkCode)) { // scroll on
            scrollMode = true;
            jFrame.setVisible(false);
            System.out.println("scroll mode on..");
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

        if (ctrlDown && Config.equal("key_d", vkCode)) {
            moveMode = true;
            jFrame.setVisible(false);
            System.out.println("move mode on..");
            return;
        }
        if (moveMode && Config.equal("key_a", vkCode)) {
            p = MouseInfo.getPointerInfo().getLocation();
            robot.mouseMove(p.x - Config.getInt("mouseStep"), p.y);
            return;
        }
        if (moveMode && Config.equal("key_s", vkCode)) {
            p = MouseInfo.getPointerInfo().getLocation();
            robot.mouseMove(p.x, p.y + Config.getInt("mouseStep"));
            return;
        }
        if (moveMode && Config.equal("key_d", vkCode)) {
            p = MouseInfo.getPointerInfo().getLocation();
            robot.mouseMove(p.x + Config.getInt("mouseStep"), p.y);
            return;
        }
        if (moveMode && Config.equal("key_w", vkCode)) {
            p = MouseInfo.getPointerInfo().getLocation();
            robot.mouseMove(p.x, p.y - Config.getInt("mouseStep"));
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
        myPanel.paint(scale, false);
        p = MouseInfo.getPointerInfo().getLocation();
        history.add(p);
    }


}
