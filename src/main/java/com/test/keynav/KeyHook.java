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
import java.util.Arrays;
import java.util.List;

/**
 * Sample implementation of a low-level keyboard hook on W32.
 */
@Deprecated
public class KeyHook {
    private static HHOOK hhk;
    private static LowLevelKeyboardProc keyboardHook;
    private static List<Integer> inputList = new ArrayList<>();
    private static Robot robot;
    private static JFrame jFrame = new JFrame();

    private static int scale = 1;
    private static List<Point> savePoints = new ArrayList<>();
    private static List<Integer> begin = Arrays.asList(162, 186); // ctrl + ;
    private static List<Integer> begin2 = Arrays.asList(162, 222); // ctrl + '
    private static List<Integer> save1 = Arrays.asList(83, 49); // s + 1
    private static List<Integer> save2 = Arrays.asList(83, 50); // s + 2
    private static List<Integer> save3 = Arrays.asList(83, 51); // s + 3
    private static List<Integer> moveLeft = Arrays.asList(162, 72); // ctrl + h
    private static List<Integer> moveDown = Arrays.asList(162, 74); // ctrl + j
    private static List<Integer> moveUp = Arrays.asList(162, 75); // ctrl + k
    private static List<Integer> moveRight = Arrays.asList(162, 76); // ctrl + l

    public static void main(String[] args) throws AWTException {
        jFrame.setTitle("key mode");
        jFrame.setSize(150, 0);
        robot = new Robot();
        final User32 lib = User32.INSTANCE;
        HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
        keyboardHook = new LowLevelKeyboardProc() {
            @Override
            public LRESULT callback(int nCode, WPARAM wParam, KBDLLHOOKSTRUCT info) {
                if (nCode >= 0) {
                    switch (wParam.intValue()) {
                        case WinUser.WM_KEYDOWN:
                            keyPressed(info.vkCode);
                            break;
                        case WinUser.WM_KEYUP:
                            inputList.clear();
                            break;
                    }
                }
                Pointer ptr = info.getPointer();
                long peer = Pointer.nativeValue(ptr);
                return lib.CallNextHookEx(hhk, nCode, wParam, new LPARAM(peer));
            }
        };
        hhk = lib.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, keyboardHook, hMod, 0);


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

    private static void keyPressed(int vkCode) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Point p = MouseInfo.getPointerInfo().getLocation();
        System.out.println("key code: " + vkCode);
        inputList.add(vkCode);
        if (begin.equals(inputList)) {
            robot.mouseMove(screenSize.width / 2, screenSize.height / 2);
            scale = 2;
            jFrame.setVisible(true);
            System.out.println("key mode on..");
        }
        if (begin2.equals(inputList)) {
            scale = 16;
            jFrame.setVisible(true);
            System.out.println("key mode on.. scale=16");
        }
        if (27 == vkCode) { // esc
            jFrame.setVisible(false);
            System.out.println("key mode off..");
        }

        boolean keyMode = jFrame.isVisible();
        if (keyMode && 13 == vkCode) { // enter: left click
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            jFrame.setVisible(false);
            System.out.println("key mode off..");
        }
        if (keyMode && 32 == vkCode) { // space: right click
            robot.mousePress(InputEvent.BUTTON3_MASK);
            robot.mouseRelease(InputEvent.BUTTON3_MASK);
        }
        if (keyMode && save1.equals(inputList)) { // save1
            savePoints.add(p);
            System.out.println("save1..");
        }
        if (keyMode && save2.equals(inputList)) { // save2
            savePoints.add(p);
            System.out.println("save2..");
        }
        if (keyMode && save3.equals(inputList)) { // save3
            savePoints.add(p);
            System.out.println("save3..");
        }
        if (keyMode && 49 == vkCode) { // restore1
            Point point = savePoints.get(0);
            robot.mouseMove(point.x, point.y);
        }
        if (keyMode && 50 == vkCode) { // restore2
            Point point = savePoints.get(0);
            robot.mouseMove(point.x, point.y);
        }
        if (keyMode && 51 == vkCode) { // restore3
            Point point = savePoints.get(0);
            robot.mouseMove(point.x, point.y);
        }

        int scaleX = screenSize.width / scale;
        int scaleY = screenSize.height / scale;
        if (keyMode && 89 == vkCode) { // Y: move left top
            robot.mouseMove(p.x - scaleX / 2, p.y - scaleY / 2);
            scale *= 2;
        }
        if (keyMode && 85 == vkCode) { // U: move right top
            robot.mouseMove(p.x + scaleX / 2, p.y - scaleY / 2);
            scale *= 2;
        }
        if (keyMode && 78 == vkCode) { // N: move left bottom
            robot.mouseMove(p.x - scaleX / 2, p.y + scaleY / 2);
            scale *= 2;
        }
        if (keyMode && 77 == vkCode) { // M: move right bottom
            robot.mouseMove(p.x + scaleX / 2, p.y + scaleY / 2);
            scale *= 2;
        }
        if (keyMode && 72 == vkCode) { // H: move left
            robot.mouseMove(p.x - scaleX / 2, p.y);
            scale *= 2;
        }
        if (keyMode && moveLeft.equals(inputList)) { // H: move left without scale down
            robot.mouseMove(p.x - scaleX / 2, p.y);
            return;
        }
        if (keyMode && 74 == vkCode) { // J: move down
            robot.mouseMove(p.x, p.y + scaleY / 2);
            scale *= 2;
        }
        if (keyMode && 75 == vkCode) { // K: move up
            robot.mouseMove(p.x, p.y - scaleY / 2);
            scale *= 2;
        }
        if (keyMode && 76 == vkCode) { // L: move right
            robot.mouseMove(p.x + scaleX / 2, p.y);
            scale *= 2;
        }

    }


}
