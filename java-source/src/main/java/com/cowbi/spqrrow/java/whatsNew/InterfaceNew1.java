package com.cowbi.spqrrow.java.whatsNew;

public interface InterfaceNew1 {

    public static final String d="";

    static void sm() {
        System.out.println("interface提供的方式实现");
    }

    default void def() {
        System.out.println("interface1默认执行的方法");
    }

    //须要实现类重写
    public abstract void f();
}
