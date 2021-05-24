package com.cowbi.spqrrow.java.hashmap;

import java.util.*;

public class LinkedHashMapTest {


    public static void main(String[] args) {

        accessOrderKey("key1");
        accessOrderKey("key2");
        accessOrderKey("key3");
    }

    private static void accessOrderKey(String key) {

        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap(16, 0.75f, true);

        linkedHashMap.put("key1", "v1");
        linkedHashMap.put("key2", "v2");
        linkedHashMap.put("key3", "v3");
        linkedHashMap.put("key4", "v4");
        linkedHashMap.put("key5", "v5");

        linkedHashMap.get(key);
        System.out.println("访问" + key + "后的顺序：");

        Set<Map.Entry<String, String>> set = linkedHashMap.entrySet();
        Iterator<Map.Entry<String, String>> iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = iterator.next();
            System.out.println("key:" + entry.getKey());
        }
    }
}
