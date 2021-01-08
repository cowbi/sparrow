package com.cowbi.spqrrow.java.hashmap;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HashMap使用
 *
 * @author zyc
 */
public class ConcurrentHashMapTest {

    public static void main(String[] args) {


        ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap(5);
        Class clazz = concurrentHashMap.getClass();
        try {
            Field field = clazz.getDeclaredField("sizeCtl");
            //打开私有访问
            field.setAccessible(true);

            //获取属性值
            Object value = field.get(concurrentHashMap);

            System.out.println("Map的初始容量=" + value);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
