package com.cowbi.spqrrow.java.threadLocal;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 实验类
 * */
public class ThreadLocalTest {

    public final int threadLocalHashCode = nextHashCode();

    /**
     * The next hash code to be given out. Updated atomically. Starts at
     * zero.
     */
    private static AtomicInteger nextHashCode =
            new AtomicInteger();

    /**
     * The difference between successively generated hash codes - turns
     * implicit sequential thread-local IDs into near-optimally spread
     * multiplicative hash values for power-of-two-sized tables.
     */
    private static final int HASH_INCREMENT = 0x61c88647;

    /**
     * Returns the next hash code.
     */
    private  int nextHashCode() {
        return nextHashCode.getAndAdd(HASH_INCREMENT);
    }


    public static void main(String[] args) {


        ThreadLocal<String> a =new ThreadLocal<>();

        a.set("a1");
        a.set("a2");
        System.out.println(a.get());
        a.remove();

        ThreadLocal<String> b =new ThreadLocal<String>();

        b.set("b1");
        b.set("b2");
        System.out.println(b.get());

        int i = nextHashCode.getAndAdd(HASH_INCREMENT);


        // Initially value as 0
        System.out.println("Current value: "
                + nextHashCode);


        // Initially value as 0
        System.out.println("Current value: "
                + i);

        int j = nextHashCode.getAndAdd(12);
        // Initially value as 0
        System.out.println("Current value: "
                + j);

        new Thread();

    }

}
