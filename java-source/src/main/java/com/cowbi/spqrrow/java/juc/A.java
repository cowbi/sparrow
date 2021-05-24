package com.cowbi.spqrrow.java.juc;

import java.nio.channels.AcceptPendingException;
import java.util.concurrent.atomic.AtomicInteger;

public class A {


    public static void main(String[] args) {


        AtomicInteger atomicInteger = new AtomicInteger(3);


        do {
            System.out.println("修改成功！");
        } while (!atomicInteger.compareAndSet(3, 4));

    }
}
