package core;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class ThreadStateTest {

    public static void main(String[] args) throws Exception {

        /*Thread t1 = new Thread(()->{
            System.out.println("Thread go!");
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });

        //NEW
        System.out.println("执行new后状态 1：：" + t1.getState());

        t1.start();

        //RUNNABLE
        System.out.println("执行start后状态 2：：" + t1.getState());

        //等待线程执行完毕
        t1.join();

        //TERMINATED
        System.out.println("执行join后的状态 3：：" + t1.getState());*/


        Thread t2 = new Thread(() -> {
            LockSupport.park();
            System.out.println("t2 run begin");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        t2.start();
        //主线程睡眠1s
        TimeUnit.SECONDS.sleep(1);
        System.out.println("t2 park：" + t2.getState());//WAITING

        LockSupport.unpark(t2);//唤醒t2
        //主线程睡眠1s 确认 t2 肯定被叫醒
        TimeUnit.SECONDS.sleep(1);
        System.out.println("t2 unpark：" + t2.getState());//TIMED_WAITING1


        //
        final Object o = new Object();
        Thread t3 = new Thread(() -> {
            synchronized (o) {
                System.out.println("t start");
            }
        });


        Thread t4 = new Thread(() -> {
            synchronized (o) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("t start");
            }
        });

        //t4 start拿到锁
        t4.start();

        //主线程睡一秒，保证t4进入睡眠状态
        Thread.sleep(1);

        //t3去竞争锁
        t3.start();

        Thread.sleep(1);

        System.out.println("t3状态：" + t3.getState());


        Thread t6 = new Thread(() -> {

            try {
                TimeUnit.SECONDS.sleep(2);
                Thread.yield();
                TimeUnit.SECONDS.sleep(5);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread t7 = new Thread(() -> {

            try {
                TimeUnit.SECONDS.sleep(10);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });


        t6.start();
        Thread.sleep(2);
        System.out.println("t6线程状态：" + t6.getState());


        System.out.println("t6线程状态：" + t6.getState());


        /*for (Thread.State state : Thread.State.values()) {
            System.out.println(state);
        }*/


    }

}