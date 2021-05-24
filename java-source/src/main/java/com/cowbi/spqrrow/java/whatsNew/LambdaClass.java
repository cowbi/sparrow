package com.cowbi.spqrrow.java.whatsNew;


import com.sun.org.apache.regexp.internal.RE;
import org.omg.PortableInterceptor.INACTIVE;

import javax.jws.soap.SOAPBinding;
import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;

/**
 * Lambada表达式
 */
public class LambdaClass extends LambdaClassSuper {


    public static LambdaInterface staticF() {
        return null;
    }

    public LambdaInterface f() {
        return null;
    }

     void show() {
        //1.调用静态函数，返回类型必须是functional-interface
        LambdaInterface t = LambdaClass::staticF;

        //2.实例方法调用
        LambdaClass lambdaClass = new LambdaClass();
        LambdaInterface lambdaInterface = lambdaClass::f;

        //3.超类上的方法调用
        LambdaInterface superf = super::sf;

        //构造方法调用
        LambdaInterface tt = LambdaClassSuper::new;

    }


    static void lambdaInterfaceDemo(LambdaInterface i) {
        System.out.println(i);
    }


    static void mathTest(LambdaInterface i) {

        System.out.println(i);


        Map<Integer, String> map = new HashMap<>();
        map.forEach((k,v)->System.out.println(v));
    }

    public static void main(String[] args) {
        LambdaClass lambdaClass = new LambdaClass();

        lambdaClass.show();
    }

    void threadTest() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("The old runable now is using!");
            }
        }).start();

        new Thread(() -> System.out.println("Lambda runable now is using!"));
    }

    static void comperatorTest() {

        List<Integer> strings = Arrays.asList(1, 2, 3);

        final int i = 0;

        Collections.sort(strings, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - i;
            }
        });

        //Lambda
        Collections.sort(strings, (Integer o1, Integer o2) -> o1 - i);


        Comparator<Integer> comperator = (Integer o1, Integer o2) -> o1 - o2;
        Collections.sort(strings, comperator);


    }

    void listenerTest() {

        JButton button = new JButton();
        button.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                e.getItem();
            }
        });

        //lambda
        button.addItemListener(e -> e.getItem());
    }


}
