# LinkedHashMap如何做到有序？

## 简介

在开发中最常用到的Map就是HashMap，多线程并发的情况并不多。有时我们需要保证Map的插入顺序和访问顺序一致，这时可能需要用到LinkedHashMap。它是HashMap的子类，大部分特征与HashMap一致，比如非线程安全，默认容量是16，扩展因子是0.75，容量必须是2的幂次方等。这些特征在 [HashMap的工作原理](https://mp.weixin.qq.com/s?__biz=MzIwNDc4Nzg3OA==&mid=2247483704&idx=1&sn=f2752a02b84f63d3c30e1c62ffdea607&chksm=973b98f1a04c11e71063ec6bb8bede27f1665f93bbb063184c9f2fb082f8c7f3b128a8b00fdb&scene=21#wechat_redirect)一文中有详细介绍，本文不做赘述。本文主要通过讲解LinkedHashMap重新和新增的方法，来揭秘LinkedHashMap的特点。如无特殊说明，源码出至jdk1.8。

## 何谓有序

LinkedHashMap的有序其实包括2种

1. 插入顺序：很好理解，就是按插入顺序储存。
2. 访问顺序，下文详解。

## 源码解析

* 我们以不带参的构造函数作为研究源码的入口。

  ```java
   /**
     * Constructs an empty insertion-ordered <tt>LinkedHashMap</tt> instance
     * with the default initial capacity (16) and load factor (0.75).
     * 没有参数的构造方法，
     * 调用父类构造函数，初始容量16，负载因子0.75
     * 按插入顺序排序
     */
    public LinkedHashMap() {
        super();
        //按插入顺序排序
        accessOrder = false;
    }

   /**
     * The iteration ordering method for this linked hash map: <tt>true</tt>
     * for access-order, <tt>false</tt> for insertion-order.
     * 排序方式，true：访问顺序；false：插入顺序，默认
     * @serial
     */
    final boolean accessOrder;
  ```

* put方法，*LinkedHashMap*并没有重写`put()` ，但是提供了put方法调用的`newNode(int hash, K key, V value, Node<K,V> e)`, 我们来看一下相关方法。

  ```java
     /**
       * HashMap.Node subclass for normal LinkedHashMap entries.
       * 有自己的Entry，继承HashMap.Node，新增before和after。其实就是双向链表
       */
      static class Entry<K,V> extends HashMap.Node<K,V> {
          Entry<K,V> before, after;
          Entry(int hash, K key, V value, Node<K,V> next) {
              super(hash, key, value, next);
          }
      }
  
      /**
       * The head (eldest) of the doubly linked list.
       * 双向链表的头部
       */
      transient LinkedHashMap.Entry<K,V> head;
  
      /**
       * The tail (youngest) of the doubly linked list.
       * 双向链表的尾部
       */
      transient LinkedHashMap.Entry<K,V> tail;
   
      /**
      * putVal调用
      */
      Node<K,V> newNode(int hash, K key, V value, Node<K,V> e) {
          LinkedHashMap.Entry<K,V> p =
              new LinkedHashMap.Entry<K,V>(hash, key, value, e);
          linkNodeLast(p);
          return p;
      }
    
      /**
      * link at the end of list
      */ 
      private void linkNodeLast(LinkedHashMap.Entry<K,V> p) {
          LinkedHashMap.Entry<K,V> last = tail;
          tail = p;
          if (last == null)
              head = p;
          else {
              p.before = last;
              last.after = p;
          }
      }
  ```

​       源码比较简单，关键部分也加了注释，有不明白的同学可以留言。从源码可以得知，LinkedHashMap的数据结构就是Linked + HashMap，即双向链表+HashMap（数组+链表+红黑树）。保证LinkedHashMap顺序的是Linked，其他和key相关的操作还是和HashMap相关。

* 遍历，既然是Linked保证了元素的顺序，那遍历也一定是和Linked相关，这里注意，不管是Linked，还是HashMap，存的都是元素的内存地址，真正对象还是在内存中。看一下LinkedHashMap遍历的过程。

  ```java
  public static void main(String[] args) {
  
          LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap();
  
          linkedHashMap.put("key1", "v1");
          linkedHashMap.put("key2", "v2");
          linkedHashMap.put("key3", "v3");
  
          System.out.println("插入顺序");
  
          Set<Map.Entry<String, String>> set = linkedHashMap.entrySet();
          Iterator<Map.Entry<String, String>> iterator = set.iterator();
          while(iterator.hasNext()) {
              Map.Entry entry = iterator.next();
              String key = (String) entry.getKey();
              String value = (String) entry.getValue();
              System.out.println("key:" + key + ",value:" + value);
          }
  
      }
   //LinkedHashMap 类
   1. 获取到Set，返回new LinkedEntrySet()
     public Set<Map.Entry<K,V>> entrySet() {
          Set<Map.Entry<K,V>> es;
          return (es = entrySet) == null ? (entrySet = new LinkedEntrySet()) : es;
     }
   2. 调用LinkedEntrySet.iterator(),返回LinkedEntryIterator
    final class LinkedEntrySet extends AbstractSet<Map.Entry<K,V>> {
          public final int size()                 { return size; }
          public final void clear()               { LinkedHashMap.this.clear(); }
          public final Iterator<Map.Entry<K,V>> iterator() {
              return new LinkedEntryIterator();
          }
          /**省略无关代码....**/
      }
   3. LinkedEntryIterator.next(),调用父类LinkedHashIterator.nextNode()返回LinkedHashMap.Entry<K,V>
    final class LinkedEntryIterator extends LinkedHashIterator
          implements Iterator<Map.Entry<K,V>> {
          public final Map.Entry<K,V> next() { return nextNode(); }
    }
    // Iterators
    abstract class LinkedHashIterator {
          LinkedHashMap.Entry<K,V> next;
          LinkedHashMap.Entry<K,V> current;
          int expectedModCount;
  
          LinkedHashIterator() {
              next = head;
              expectedModCount = modCount;
              current = null;
          }
  
          public final boolean hasNext() {
              return next != null;
          }
  
          final LinkedHashMap.Entry<K,V> nextNode() {
              //第一次遍历next=head，在构造方法中提现
              LinkedHashMap.Entry<K,V> e = next;
              if (modCount != expectedModCount)
                  throw new ConcurrentModificationException();
              if (e == null)
                  throw new NoSuchElementException();
              current = e;
              //next指向下一节点
              next = e.after;
              return e;
          }
      }
  ```

  源码中可以看出，遍历Linked就是从head属性开始，一致`next()`，直到`next==null`。

* 按访问顺序排序是什么鬼？访问顺序也就是`get(key)`方法的执行顺序。我们先搞个测试用例，把accessOrder设置成true，看输出结果。

  ```java
    测试调用LinkedHashMap的构造函数是：
    public LinkedHashMap(int initialCapacity,
                           float loadFactor,
                           boolean accessOrder)
      
      
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
  //执行结果
  访问key1后的顺序：
  key:key2
  key:key3
  key:key4
  key:key5
  key:key1
  访问key2后的顺序：
  key:key1
  key:key3
  key:key4
  key:key5
  key:key2
  访问key3后的顺序：
  key:key1
  key:key2
  key:key4
  key:key5
  key:key3
  ```

  从测试的结果来看，貌似刚被访问完的元素放在linked的尾端，再看`get()`搞了什么鬼？

  ```java
  public V get(Object key) {
          Node<K,V> e;
          if ((e = getNode(hash(key), key)) == null)
              return null;
          //accessOrder = true,调整Linked顺序
          if (accessOrder)
              afterNodeAccess(e);
          return e.value;
  }
  // move node to last
  void afterNodeAccess(Node<K,V> e) {
          LinkedHashMap.Entry<K,V> last;
          //accessOrder=true e不是最末尾
          if (accessOrder && (last = tail) != e) {
              //当e.key == key1时,下面应key1代表其对应的node。
              LinkedHashMap.Entry<K,V> p =(LinkedHashMap.Entry<K,V>)e, //key1
              b = p.before, //null
              a = p.after; //key2
              p.after = null;
              if (b == null)//条件成立，head=a，也就是head=key2
                  head = a;
              else
                  b.after = a;
            
              if (a != null)//a.before = null
                  a.before = b;
              else
                  last = b;
            
              if (last == null)
                  head = p;
              else {
                  //此时last=key5，
                  //p.before=last=key5，key5.after=p 双向链表
                  p.before = last;
                  last.after = p;
              }
              //p赋值给链表末尾，也就是把key1放到了链表的末尾
              tail = p;
              ++modCount;
          }
      }
  ```

  果然和我们预料的一样，最新访问的元素放到链表的最末端。`afterNodeAccess()`的注释也说的很清楚：move node to last。这时注释的重要性就体现出来了。

## 总结

我们从LinkedHashMap源码的角度分析了LinkedHashMap的结构与原理，在此基础上也分析出它的有序性和如何做到有序性。它的结构正如其名Linked+HashMap，Linked负责顺序，HashMap复制检索。

LinkedHashMap的2种顺序说明

1. Linked按插入顺序储存：源码中当accessOrder = false时。
2. Linked按访问顺序储存：即最被访问的元素放到Linked的尾端，其他元素顺序不变。源码中当accessOrder = true。

什么是学习：每天进步一点点。哪怕是只多知道了LinkedHashMap如何实现排序。

本人水平一般，能力有限，谬误之处还请大家指出来。原创不易，记得点赞哦。