package com.cowbi.spqrrow.java.designPattern;

public class Singleton {

    //1 简单的单例模式
    private static Singleton singleton1 = new Singleton();

    private Singleton() {
    }

    public static Singleton getSingleton() {
        return singleton1;
    }

    //2 懒汉式，第1种是每次实例化都默认创建一个对象，但我们想让它在使用时才创建
    private static Singleton singleton2;

    public static Singleton getSingleton2() {
        if (singleton2 == null) { //1 线程a
            singleton2 = new Singleton();//2 线程b
        }
        return singleton2;
    }

    //3 多线程，上面2种都只考虑了单线程的情况。当线程a执行1时，线程b初始化对象还未完成。接下来a也会初始化对象，破坏单例。
    // 多线程我们自然想到的就是synchronized
    public static synchronized Singleton getSingleton3() {

        if (singleton2 == null) {
            singleton2 = new Singleton();
        }
        return singleton2;
    }

    //4 其实我们需要加锁的只有new Singleton()，其他业务其实并不需要加。所以我们可以缩小锁的粒度
    //如果频繁调用，synchronized势必影响性能。
    //双重检查锁定（double-checked locking）
    //为什么要第二次检查 线程a进来检查是null，线程b检查也是null。但是线程a先拿到锁，执行完毕后线程b拿到锁把又初始化一遍。
    //Double-checked lock
    public static Singleton getSingleton4() {

        //可能有若干业务
        if (singleton2 == null) { //4
            synchronized (Singleton.class) {
                if (singleton2 == null) {
                    singleton2 = new Singleton();//5
                }
            }
        }
        return singleton2;
    }

    //5 第四种在并发量少的时候测试没有什么问题。但面试官会问，这就完了吗？singleton2是否需要加volatile呢?
    //既然这么问了，一定要加
    private static volatile Singleton singleton5;

    //为什么要加？这里要用到volatile的避免指令重排序。

    //new对象的过程
    /**
     * 1. 分配内存
     * 2. 设置初始值 通过构造方法初始化
     * 3. 把分配的地址赋给对象
     *
     * java运行乱序。
     * 所以当一个线程执行到4的时候不为null，但是5行初始化还未完成。
     * 这样就会出现问题。还没调用构造方法就去用肯定是有问题的。
     *
     *
     * intra-thread semantics保证重排序不会改变单线程内的程序执行结果。
     * 换句话来说，intra-thread semantics允许那些在单线程内，
     * 不会改变单线程程序执行结果的重排序。上面三行伪代码的2和3之间虽然被重排序了，
     * 但这个重排序并不会违反intra-thread semantics。
     * 这个重排序在没有改变单线程程序的执行结果的前提下，可以提高程序的执行性能。
     *
     * java语言是运行重排序的。
     * 比如a=1 b=2
     * 1：a=1
     * 2：b=2
     * 把1.2 调换顺序执行，不影响结果。
     *
     * 所以2-3也是允许重新排序的。
     * */

    public static void main(String[] args) {
        Object o  = new Object();
    }


    //6不带锁的单例
    private static class Inner{
        private static Singleton singleton = new Singleton();
    }

    public Singleton getSingleton6(){
        return Inner.singleton;
    }

    //枚举 天生的单例
    public enum SingletonDemo4 {

        //枚举元素本身就是单例
        INSTANCE;

        //添加自己需要的操作
        public void singletonOperation(){
        }
    }

}



