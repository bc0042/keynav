package com.test.keynav;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
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
@Deprecated
public class KeynavBak {
    private static int ctrl = 162; // ctrl
    private static int esc = 27; // esc
    private static int enter = 13; // enter
    private static int space = 32; // space
    private static int begin = 186; // ;
    private static int begin2 = 222; // '
    private static int key1 = 49; // 1
    private static int key2 = 50; // 2
    private static int key3 = 51; // 3
    private static int upperLeft = 89; // y
    private static int upperRight = 85; // u
    private static int lowerLeft = 78; // n
    private static int lowerRight = 77; // m
    private static int left = 72; // h
    private static int down = 74; // j
    private static int up = 75; // k
    private static int right = 76; // l
    private static int back = 90; // z
    private static int forward = 88; // x
    private static int scrollOn = 79; // o
    private static int scrollDown = 219; // [
    private static int scrollUp = 221; // ]

    private static int scale = 1;
    private static int wheelAmt = 2;
    private static int historyMax = 99;
    private static boolean ctrlDown = false;
    private static boolean scrollMode = false;
    private static HHOOK hhk;
    private static LowLevelKeyboardProc keyboardHook;
    private static Robot robot;
    private static JFrame jFrame = new JFrame();
    private static List<Point> savePoints = new ArrayList<>();
    private static LinkedList<Point> history = new LinkedList<>();
    private static MyPanel myPanel;


    public static void main(String[] args) throws AWTException {
        myPanel = new MyPanel();
        jFrame.add(myPanel);
        jFrame.setUndecorated(true);
        jFrame.setOpacity(0.2f);
        jFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        robot = new Robot();
        final User32 lib = User32.INSTANCE;
        HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
        keyboardHook = new LowLevelKeyboardProc() {
            @Override
            public LRESULT callback(int nCode, WPARAM wParam, KBDLLHOOKSTRUCT info) {
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
            }
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
        if (ctrl == vkCode) {
            ctrlDown = false;
        }
    }

    private static void keyDown(int vkCode) {
        boolean keyMode = jFrame.isVisible();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Point p = MouseInfo.getPointerInfo().getLocation();
        int scaleX = screenSize.width / scale;
        int scaleY = screenSize.height / scale;
        System.out.println("key code: " + vkCode);

        if (ctrl == vkCode) {
            ctrlDown = true;
            return;
        }
        if (ctrlDown && begin == vkCode) {
            robot.mouseMove(screenSize.width / 2, screenSize.height / 2);
            myPanel.paint(1);
            jFrame.setVisible(true);
            scale = 2;
            System.out.println("key mode on..");
            return;
        }
        if (ctrlDown && begin2 == vkCode) {
            myPanel.paint(8);
            jFrame.setVisible(true);
            scale = 16;
            System.out.println("key mode on.. scale=16");
            return;
        }
        if (esc == vkCode) {
            jFrame.setVisible(false);
            System.out.println("key mode off..");
            return;
        }

        // key mode on
        if (keyMode && enter == vkCode) { //left click
            jFrame.setVisible(false);
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            System.out.println("key mode off..");
            return;
        }
        if (keyMode && space == vkCode) { // right click
            jFrame.setVisible(false);
            robot.mousePress(InputEvent.BUTTON3_MASK);
            robot.mouseRelease(InputEvent.BUTTON3_MASK);
            return;
        }
        if (keyMode && ctrlDown && key1 == vkCode) {
            savePoints.add(p);
            System.out.println("key1..");
            return;
        }
        if (keyMode && ctrlDown && key2 == vkCode) {
            savePoints.add(p);
            System.out.println("key2..");
            return;
        }
        if (keyMode && ctrlDown && key3 == vkCode) {
            savePoints.add(p);
            System.out.println("key3..");
            return;
        }
        if (keyMode && key1 == vkCode) { // restore1
            if (savePoints.size() >= 1) {
                Point point = savePoints.get(0);
                robot.mouseMove(point.x, point.y);
            }
            return;
        }
        if (keyMode && key2 == vkCode) { // restore2
            if (savePoints.size() >= 2) {
                Point point = savePoints.get(1);
                robot.mouseMove(point.x, point.y);
            }
            return;
        }
        if (keyMode && key3 == vkCode) { // restore3
            if (savePoints.size() >= 3) {
                Point point = savePoints.get(2);
                robot.mouseMove(point.x, point.y);
            }
            return;
        }

        // upperLeft upperRight lowerLeft lowerRight
        if (keyMode && upperLeft == vkCode) {
            robot.mouseMove(p.x - scaleX / 2, p.y - scaleY / 2);
            myPanel.paint(scale);
            addHistory();
            scale *= 2;
            return;
        }
        if (keyMode && upperRight == vkCode) {
            robot.mouseMove(p.x + scaleX / 2, p.y - scaleY / 2);
            myPanel.paint(scale);
            addHistory();
            scale *= 2;
            return;
        }
        if (keyMode && lowerLeft == vkCode) {
            robot.mouseMove(p.x - scaleX / 2, p.y + scaleY / 2);
            myPanel.paint(scale);
            addHistory();
            scale *= 2;
            return;
        }
        if (keyMode && lowerRight == vkCode) {
            robot.mouseMove(p.x + scaleX / 2, p.y + scaleY / 2);
            myPanel.paint(scale);
            addHistory();
            scale *= 2;
            return;
        }
        // ctrl + left down up right
        if (keyMode && ctrlDown && left == vkCode) {
            robot.mouseMove(p.x - scaleX / 2, p.y);
            myPanel.paint(scale);
            return;
        }
        if (keyMode && ctrlDown && down == vkCode) {
            robot.mouseMove(p.x, p.y + scaleY / 2);
            myPanel.paint(scale);
            return;
        }
        if (keyMode && ctrlDown && up == vkCode) {
            robot.mouseMove(p.x, p.y - scaleY / 2);
            myPanel.paint(scale);
            return;
        }
        if (keyMode && ctrlDown && right == vkCode) {
            robot.mouseMove(p.x + scaleX / 2, p.y);
            myPanel.paint(scale);
            return;
        }
        // left down up right
        if (keyMode && left == vkCode) {
            robot.mouseMove(p.x - scaleX / 2, p.y);
            myPanel.paint(scale);
            addHistory();
            scale *= 2;
            return;
        }
        if (keyMode && down == vkCode) {
            robot.mouseMove(p.x, p.y + scaleY / 2);
            myPanel.paint(scale);
            addHistory();
            scale *= 2;
            return;
        }
        if (keyMode && up == vkCode) {
            robot.mouseMove(p.x, p.y - scaleY / 2);
            myPanel.paint(scale);
            addHistory();
            scale *= 2;
            return;
        }
        if (keyMode && right == vkCode) {
            robot.mouseMove(p.x + scaleX / 2, p.y);
            myPanel.paint(scale);
            addHistory();
            scale *= 2;
            return;
        }
        // move back
        if (keyMode && ctrlDown && back == vkCode) {
            Point last = history.removeLast();
            history.addFirst(last);
            robot.mouseMove(last.x, last.y);
        }
        // move forward
        if (keyMode && ctrlDown && forward == vkCode) {
            Point first = history.removeFirst();
            history.addLast(first);
            robot.mouseMove(first.x, first.y);
        }
        // scroll
        if (keyMode && scrollOn == vkCode) {
            scrollMode = true;
        }
        if (scrollMode && scrollDown == vkCode) {
            robot.mouseWheel(wheelAmt);
        }
        if (scrollMode && scrollUp == vkCode) {
            robot.mouseWheel(-wheelAmt);
        }

    }

    private static void addHistory() {
        Point p = MouseInfo.getPointerInfo().getLocation();
        if (history.size() >= historyMax) {
            history.removeFirst();
        }
        history.add(p);
    }

}
