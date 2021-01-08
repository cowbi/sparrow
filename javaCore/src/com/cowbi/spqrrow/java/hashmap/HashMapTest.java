package com.cowbi.spqrrow.java.hashmap;

import java.util.HashMap;
import java.util.UUID;

/**
 * HashMap使用
 *
 * @author zyc
 * */
public class HashMapTest {


    public static void main(String[] args) {

        //hashmap初始容量initialCapacity必须是2的n次幂。如果不是呢？
        HashMap hashMap = new HashMap(7);
        hashMap.put("3","3");
        Object object = hashMap.get("3");
        System.out.println(object.toString());
    }

    void f() throws InterruptedException {

        /**
         *
         * 1.7 & 1.8
         * 在上面的例子中，我们利用for循环，启动了10000个线程，每个线程都向共享变量中添加一个元素。
         *
         * 测试结果：通过使用JDK自带的jconsole工具，可以看到HashMap内部形成了死循环，并且主要集中在两处代码上
         *
         * */
        final HashMap<String,String> map = new HashMap<String,String>();

        Thread t = new Thread(new Runnable(){
            @Override
            public  void run(){

                for(int x=0;x<10000;x++){
                    Thread tt = new Thread(new Runnable(){
                        @Override
                        public void run(){
                            map.put(UUID.randomUUID().toString(),"");
                        }
                    });
                    tt.start();
                    System.out.println(tt.getName());
                }
            }
        });
        t.start();
        t.join();
    }
}
