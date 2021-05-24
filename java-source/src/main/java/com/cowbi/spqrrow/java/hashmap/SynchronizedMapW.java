package com.cowbi.spqrrow.java.hashmap;

import java.io.Serializable;
import java.util.*;

/**
 * 实现hashMap线程安全的方法,只实现了put和get方法。其他同理。
 * @see Collections#synchronizedMap(Map)
 *
 * @author zyc
 */
public class SynchronizedMapW<K, V> implements Map<K,V>, Serializable {

    private static final long serialVersionUID = 6014923206678367488L;

    public static void main(String[] args) {

        HashMap<String, String> hashMap = new HashMap(2);

        //等同于HashTable，只是对当前传入的map对象，新增对象锁（synchronized）：
        Map synchronizedMap = Collections.synchronizedMap(hashMap);
        synchronizedMap.put("key", "val");
        //the class implements
        SynchronizedMapW synchronizedMapW = new SynchronizedMapW(hashMap);
        synchronizedMapW.put("key","value");

    }
    /**
     *
     * Backing Map
     */
    private final Map<K, V> m;

    final Object mutex;

    SynchronizedMapW(Map<K, V> m) {
        this.m = Objects.requireNonNull(m);
        mutex = this;
    }

    @Override
    public V put(K key, V value) {
        synchronized (mutex) {return m.put(key, value);}
    }

    @Override
    public V get(Object key) {
        synchronized (mutex) {return m.get(key);}
    }

    /**
     *  ---------------- no implements -------------- */
    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }
}
