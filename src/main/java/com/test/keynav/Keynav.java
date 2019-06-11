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

import java.awt.*;

/**
 * Sample implementation of a low-level keyboard hook on W32.
 */
public class Keynav {
    private static boolean shiftDown = false;
    private static boolean ctrlDown = false;
    private static boolean altDown = false;
    private static boolean keyMode = false;
    private static boolean scrollMode = false;
    private static boolean moveMode = false;
    private static HHOOK hhk;
    private static Point[] savePoints = new Point[2];
    private static MyQueue history;
    private static MyQueue input;
    private static int scale;
    private static int fixedScale;

    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String UP = "up";
    private static final String DOWN = "down";
    private static final String UPPER_LEFT = "upperLeft";
    private static final String UPPER_RIGHT = "upperRight";
    private static final String LOWER_LEFT = "lowerLeft";
    private static final String LOWER_RIGHT = "lowerRight";


    public static void main(String[] args) {
        input = new MyQueue(2);
        history = new MyQueue(Config.getInt("historySize"));
        fixedScale = Config.getInt("fixedScale");
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
        MyDevice.init();
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
        if (Config.equal("alt", vkCode)) {
            altDown = false;
        }
        if (Config.equal("ctrl", vkCode)) {
            ctrlDown = false;
        }
        if (Config.equal("shift", vkCode)) {
            shiftDown = false;
        }

    }

    private static void keyDown(int vkCode) {
        Point p = MyRobot.currPoint();
        keyMode = MyFrame.isVisible();
        System.out.println("key code: " + vkCode);

        if (Config.equal("alt", vkCode)) {
            altDown = true;
            return;
        }
        if (Config.equal("ctrl", vkCode)) {
            ctrlDown = true;
            return;
        }
        if (Config.equal("shift", vkCode)) {
            shiftDown = true;
            return;
        }
        if (ctrlDown && Config.equal("begin", vkCode)) { //begin
            scale = 1;
            MyRobot.move2Start();
            MyFrame.showAndPaint(scale, true);
            input.clear();
            System.out.println("key mode on..");
            return;
        }
        if (ctrlDown && Config.equal("begin2", vkCode)) { //begin2
            scale = fixedScale;
            MyFrame.showAndPaint(fixedScale, false);
            System.out.println("key mode on.. scale=" + scale);
            return;
        }
        if (Config.equal("esc", vkCode)) { //escape
            MyFrame.setVisible(false);
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
                MyRobot.mouseMove(restore.x, restore.y);
                input.clear();
                MyFrame.paint(fixedScale, false);
            }
            return;
        }

        if ((keyMode || moveMode) && Config.equal("key_e", vkCode)) { //left leftClick with delay
            MyFrame.setVisible(false);
            MyRobot.leftClick(10);
            return;
        }
        if (keyMode && Config.equal("enter", vkCode)) { //left leftClick
            MyFrame.setVisible(false);
            MyRobot.leftClick(0);
            System.out.println("key mode off..");
            return;
        }
        if ((keyMode || moveMode) && Config.equal("key_q", vkCode)) { //right leftClick
            MyFrame.setVisible(false);
            MyRobot.rightClick(0);
            return;
        }
        if (ctrlDown && altDown && Config.equal("key1", vkCode)) { // restore1
            Point point = savePoints[0];
            if (point != null) {
                MyRobot.mouseMove(point.x, point.y);
            }
            return;
        }
        if (ctrlDown && altDown && Config.equal("key2", vkCode)) { // restore2
            Point point = savePoints[1];
            if (point != null) {
                MyRobot.mouseMove(point.x, point.y);
            }
            return;
        }
        if (ctrlDown && Config.equal("key1", vkCode)) { // save1
            savePoints[0] = p;
            System.out.println("save1..");
            return;
        }
        if (ctrlDown && Config.equal("key2", vkCode)) { // save2
            savePoints[1] = p;
            System.out.println("save2..");
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
            MyRobot.mouseMove(last.x, last.y);
            return;
        }
        if (keyMode && ctrlDown && Config.equal("forward", vkCode)) { // move forward
            Point first = (Point) history.removeFirst();
            history.addLast(first);
            MyRobot.mouseMove(first.x, first.y);
            return;
        }

        if (ctrlDown && Config.equal("scrollDown", vkCode)) { // scroll on
            scrollMode = true;
            System.out.println("scroll mode on..");
            return;
        }
        if (scrollMode && Config.equal("scrollDown", vkCode)) { // scroll down
            MyRobot.mouseWheel(Config.getInt("wheelAmt"));
            return;
        }
        if (scrollMode && Config.equal("scrollUp", vkCode)) { // scroll up
            MyRobot.mouseWheel(-Config.getInt("wheelAmt"));
            return;
        }
        if (keyMode && Config.equal("key_w", vkCode)) { // start move mode
            moveMode = true;
            MyFrame.setVisible(false);
            System.out.println("move mode on..");
            return;
        }
        if (moveMode && Config.equal("key_a", vkCode)) {
            p = MyRobot.currPoint();
            MyRobot.mouseMove(p.x - Config.getInt("mouseStep"), p.y);
            return;
        }
        if (moveMode && Config.equal("key_s", vkCode)) {
            p = MyRobot.currPoint();
            MyRobot.mouseMove(p.x, p.y + Config.getInt("mouseStep"));
            return;
        }
        if (moveMode && Config.equal("key_d", vkCode)) {
            p = MyRobot.currPoint();
            MyRobot.mouseMove(p.x + Config.getInt("mouseStep"), p.y);
            return;
        }
        if (moveMode && Config.equal("key_w", vkCode)) {
            p = MyRobot.currPoint();
            MyRobot.mouseMove(p.x, p.y - Config.getInt("mouseStep"));
            return;
        }
    }

    private static void move(String flag) {
        Point p = MyRobot.currPoint();
        if (!ctrlDown) {
            scale *= 2;
        }
        int x = 0, y = 0;
        int offsetX = (MyDevice.width / scale / 2);
        int offsetY = (MyDevice.height / scale / 2);
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
        MyRobot.mouseMove(x, y);
        MyFrame.paint(scale, false);
        p = MyRobot.currPoint();
        history.add(p);
    }


}
