package com.cowbi.spqrrow.java.whatsNew;

@FunctionalInterface
public interface InterfaceNew {

    static void sm() {
        System.out.println("interface提供的方式实现");
    }

    default void def() {
        System.out.println("interface默认执行的方法");
    }

    //须要实现类重写
    void f();
}
