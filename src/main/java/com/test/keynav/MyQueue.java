package com.test.keynav;

import java.util.LinkedList;

public class MyQueue extends LinkedList {
    private static int max;

    public MyQueue(int size) {
        this.max = size;
    }

    @Override
    public boolean add(Object o) {
        if (this.size() >= max) {
            this.removeFirst();
        }
        return super.add(o);
    }
}
