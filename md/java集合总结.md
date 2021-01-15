### 简介

集合在任何语言都有非常广泛的应用，不同集合底层对应的数据结构和算法决定了它的特征，面试中总会被问到各个集合之间的区别和它们的特点，其实了解底层数据结构和算法后这些问题都会引刃而解，万变不离其宗。本文尝试从底层剖析主流集合的底层结构与实现原理，如无特殊说明，本文源码出自jdk1.8。

### 总揽

java集合架构图，来源于网络

![](/Users/zhaoyancheng/Downloads/素材/集合框架.gif)

java集合的2个顶级接口Collection和Map。

![](/Users/zhaoyancheng/Downloads/java集合.png)

话不多说，接下来我们对它们作一一介绍。

#### Map

先对java.util.Map的4个常用实现类做简单的介绍及对比。

#####  HashTable

比较古老，从JDK1.0就开始有了。线程安全，操作时锁整个表，效率低，现在基本被遗弃。

##### HashMap
毫不谦虚的说，这是最常用的map，也是面试中最常被问到的map。它的特点主要有：
  * 非线程安全，可以用 Collections.synchronizedMap(m)方法使HashMap具有线程安全的能力，或者直接使用ConcurrentHashMap
  * 无序
  * 底层数据结构是**数组+链表+红黑树**
  * 允许一个key为null，并把它放在第一个bucket。允许多个value为null。
  * 更多内容请看之前发的文章[搞定HashMap面试，深入讲解HashMap的工作原理](http://mp.weixin.qq.com/s?__biz=MzIwNDc4Nzg3OA==&mid=100000055&idx=1&sn=2b73c786bd5f4e97c3a6d396c9f7ef8a&chksm=173b98fe204c11e8f3a37ef693f97f98b28f357ac371169b2ede1a8e49f616484ed01d0fe276#rd)
##### LinkedHashMap
是HashMap子类，同样是非线程安全，key可以为null，但它有序。LinkedHashMap可以看成是**HashMap+LinkedList**，使用HashMap操作数据结构，用LinkedList维护插入元素的先后顺序。这篇文章对它讲的比较详细[[java集合之LinkedHashMap](https://www.cnblogs.com/xiaoxi/p/6170590.html)](https://www.cnblogs.com/xiaoxi/p/6170590.html)
##### TreeMap
有序的map，key不允许是null。其中key必须实现Comparable接口或者在构造TreeMap传入自定义的Comparator，否则会在运行时抛出java.lang.ClassCastException类型的异常。数字类型（Integer、Long、Float、Double、BigDecimal、BigInteger）和String、Date、Boolean等都实现了Comparable接口。


#### Set

##### HashSet

```java
   /**
     * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
     * default initial capacity (16) and load factor (0.75).
     */
    public HashSet() {
        map = new HashMap<>();
    }
   
   /**
     * Adds the specified element to this set if it is not already present.
     * More formally, adds the specified element <tt>e</tt> to this set if
     * this set contains no element <tt>e2</tt> such that
     * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>.
     * If this set already contains the element, the call leaves the set
     * unchanged and returns <tt>false</tt>.
     *
     * @param e element to be added to this set
     * @return <tt>true</tt> if this set did not already contain the specified
     * element
     */
    public boolean add(E e) {
        return map.put(e, PRESENT)==null;
    }
		// Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();
```

从`HashSet`的构造函数和`add()`  的源码可以看出它基于HashMap，值是HashMap的key。这也就不难理解为什么`HashSet`的值不能重复，无序了。

##### LinkedHashSet

```java
public class LinkedHashSet<E>
    extends HashSet<E>
    implements Set<E>, Cloneable, java.io.Serializable {
  
    public LinkedHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor, true);
    }
    
    public LinkedHashSet() {
        super(16, .75f, true);
    }
  
    public LinkedHashSet(int initialCapacity) {
        super(initialCapacity, .75f, true);
    }
}
public class HashSet<E> extends AbstractSet<E>
    implements Set<E>, Cloneable, java.io.Serializable
{
   HashSet(int initialCapacity, float loadFactor, boolean dummy) {
        map = new LinkedHashMap<>(initialCapacity, loadFactor);
    }
}
```

系HashSet子类，有序。`LinkedHashSet`所有构造方法都调用父类`HashSet(int initialCapacity, float loadFactor, boolean dummy)`，初始化了一个LinkedHashMap，后续操作也是基于LinkedHashMap。所以它的特点也是基于LinkedHashMap。

##### TreeSet

```java
public class TreeSet<E> extends AbstractSet<E> implements NavigableSet<E>, Cloneable, java.io.Serializable
{
    /**
     * The backing map.
     */
    private transient NavigableMap<E,Object> m;

    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();

    /**
     * Constructs a set backed by the specified navigable map.
     */
    TreeSet(NavigableMap<E,Object> m) {
        this.m = m;
    }
  
    public TreeSet() {
        this(new TreeMap<E,Object>());
    }
  
  	public boolean add(E e) {
        return m.put(e, PRESENT)==null;
    }
}
```

从`TreeSet`构造函数和`add()`源码可以看出，它基于TreeMap，它的值就是TreeMap的key，要求同样是必须实现Comparable接口或者在构造TreeMap传入自定义的Comparator，否则会在运行时抛出java.lang.ClassCastException类型的异常。

#### List

##### ArrayList

这是开发中最常用的数组，本质上就是一个Object[]。其特点有

* 有序，可重复

* 查询快，增删慢

* 非线程安全

* 容量，默认是10。扩容参见源码`grow(int minCapacity)`方法。

  ```java
  （1） public boolean add(E e) {
          //先检查是否需要扩容，再新增元素
          ensureCapacityInternal(size + 1);  // Increments modCount!!
          elementData[size++] = e;
          return true;
    }
  （2） private void ensureCapacityInternal(int minCapacity) {
          ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }
  
   （3）private static int calculateCapacity(Object[] elementData, int minCapacity) {
          if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
              return Math.max(DEFAULT_CAPACITY, minCapacity);
          }
          return minCapacity;
    }
   （4）private void ensureExplicitCapacity(int minCapacity) {
          modCount++;
          // overflow-conscious code
          //一般情况下minCapacity =（size+1），size是list.size(),也就是
          if (minCapacity - elementData.length > 0)
              grow(minCapacity);
      }
   （5）private void grow(int minCapacity) {
          // overflow-conscious code
          int oldCapacity = elementData.length;
          //新数组长度是老数组长度+（老数组长度除以2）的int值
          int newCapacity = oldCapacity + (oldCapacity >> 1);
          if (newCapacity - minCapacity < 0)
              newCapacity = minCapacity;
          if (newCapacity - MAX_ARRAY_SIZE > 0)
              newCapacity = hugeCapacity(minCapacity);
          // minCapacity is usually close to size, so this is a win:
          elementData = Arrays.copyOf(elementData, newCapacity);
      }
    （6）private static int hugeCapacity(int minCapacity) {
          if (minCapacity < 0) // overflow
              throw new OutOfMemoryError();
          return (minCapacity > MAX_ARRAY_SIZE) ?
              Integer.MAX_VALUE :
              MAX_ARRAY_SIZE;
      }
  ```

  从源码中可以清楚的看出扩容机制，很多博主说扩容是原来容量的1.5倍，显然是不严谨的，甚至是错误的，比如下面的情况。

  ```java
  //ArrayList带初始化容量的构造方法，其中elementData是底层Object[]，它的长度就是ArrayList的容量。而ArrayList.size是ArrayList实际放了多少个元素。
  public ArrayList(int initialCapacity) {
          if (initialCapacity > 0) {
              this.elementData = new Object[initialCapacity];
          } else if (initialCapacity == 0) {
              this.elementData = EMPTY_ELEMENTDATA;
          } else {
              throw new IllegalArgumentException("Illegal Capacity: "+
                                                 initialCapacity);
          }
  }
  
  //测试类
  public static void main(String[] args) {
  				//ArrayList初始化容量是1
          ArrayList list = new ArrayList(1);
         
          list.add(1);
    			//新增第二个元素的时候需要扩容
          list.add(2);
          Class clazz = list.getClass();
          try {
              Field field = clazz.getDeclaredField("elementData");
              field.setAccessible(true);
              //获取并打印elementData.length也就是ArrayList容量
              Object[] value = (Object[])field.get(list);
              System.out.println(value.length);
          } catch (Exception e) {
              e.printStackTrace();
          }
    }      
  // 最终输出ArrayList数组的容量是2，并非1*1.5=3。
  // 小红还测试了初始容量是2
  ```

* ArrayList批量remove的问题

  ```java
  public static void main(String[] args) {
  
          ArrayList<String> list = new ArrayList(Arrays.asList("0","1","2","3","4","5"));
          System.out.println("list:" + list); 
          for (int i = 0; i < list.size(); i++) {
              list.remove(i);
          }
          System.out.println("删除后list：" + list);
   }       
  /**打印结果
    list:[0, 1, 2, 3, 4, 5]
    删除后list：[1, 3, 5]
    开始设想是remove所有元素，但是结果让人意外，出现漏删情况
   */
   //list.remove(i)源码
   public E remove(int index) {
            //检查index是否合法
            rangeCheck(index);
            modCount++;
            //要删除的元素值
            E oldValue = elementData(index);
            int numMoved = size - index - 1;
            if (numMoved > 0)
                System.arraycopy(elementData, index+1, elementData, index,
                                 numMoved);
            elementData[--size] = null; // clear to let GC do its work
    
            return oldValue;
        }
    /**
     上面的for循环。当i=0时能正常删除index=0的元素。数组变为[1, 2, 3, 4, 5]
    当i=1时，删除的是新数组[1, 2, 3, 4, 5]中index=1的元素，也就是2，所以元素1就被漏掉了。同理[1, 3, 5]都没被删除掉。
    这种情况可以用倒叙删除解决，如下代码：*/
    
      for (int i = list.size()-1; i>= 0; i--) {
          list.remove(i);
      }
  ```
  
  用传统的for删除元素会出现漏删，那用foreach呢？
  
  ```java
  public static void main(String[] args) {
  
          ArrayList<String> list = new ArrayList(Arrays.asList("0","1","2","3","4","5"));
  
          System.out.println("list:" + list);
  
          for (String s:list) {
              list.remove(s);
          }
  
          System.out.println("删除后list：" + list);
      }
  //结果更糟糕，直接抛异常：Exception in thread "main" java.util.ConcurrentModificationException
  ```
  
    foreach，内部是用Iterator实现的，一次循环完了会调用ArrayList内部实现类Itr的next()方法，移至下一个元素，异常也是出现在这个地方。
  
  ```java
  public class ArrayList<E> extends AbstractList<E>
          implements List<E>, RandomAccess, Cloneable, java.io.Serializable
    {
         public boolean remove(Object o) {
            if (o == null) {
                for (int index = 0; index < size; index++)
                    if (elementData[index] == null) {
                        fastRemove(index);
                        return true;
                    }
            } else {
                for (int index = 0; index < size; index++)
                    if (o.equals(elementData[index])) {
                        fastRemove(index);
                        return true;
                    }
            }
            return false;
        }
    
    private void fastRemove(int index) {
            //modCount++ 值已经修改
            modCount++;
            int numMoved = size - index - 1;
            if (numMoved > 0)
                System.arraycopy(elementData, index+1, elementData, index,
                                 numMoved);
            elementData[--size] = null; // clear to let GC do its work
    }
    //在arraylist内部类Itr.next()首先检查 checkForComodification(),也就是modCount和expectedModCount是否相等。
    //最开始expectedModCount值就是modCount，但在#fastRemove方法中modCount已经改变，所以此刻它俩并不相等，所以会抛出异常     ConcurrentModificationException
      //而用Itr的remove()把expectedModCount = modCount，所以不抛异常。并且会把删除的节点赋值给cursor，当遍历的时候，也就不会发生漏掉的情况。
       private class Itr implements Iterator<E> {
            int cursor;       // index of next element to return
            int lastRet = -1; // index of last element returned; -1 if no such
            int expectedModCount = modCount;
    
            Itr() {}
    
            public boolean hasNext() {
                return cursor != size;
            }
    
            @SuppressWarnings("unchecked")
            public E next() {
                checkForComodification();
                int i = cursor;
                if (i >= size)
                    throw new NoSuchElementException();
                Object[] elementData = ArrayList.this.elementData;
                if (i >= elementData.length)
                    throw new ConcurrentModificationException();
                cursor = i + 1;
                return (E) elementData[lastRet = i];
            }
      			final void checkForComodification() {
                if (modCount != expectedModCount)
                    throw new ConcurrentModificationException();
            }
            //Iterator的remove()
            public void remove() {
                if (lastRet < 0)
                    throw new IllegalStateException();
                checkForComodification();
    
                try {
                    ArrayList.this.remove(lastRet);
                    cursor = lastRet;
                    lastRet = -1;
                    //关键步骤设置expectedModCount = modCount
                    expectedModCount = modCount;
                } catch (IndexOutOfBoundsException ex) {
                    throw new ConcurrentModificationException();
                }
            }
       }
    }
  ```
  
  总结：
  
  1. 普通for循环i++方式remove元素会出现漏删，修改为i--的方式
  2. 用foreach操作remove，add都会抛        java.util.ConcurrentModificationException。修改为用Iterator方式删除。

##### Vector

底层也是Object[]，特点有

* 有序，可重复

* 查询快，增删慢

* 线程安全

* 扩容 ,  同样说Vector扩容是原来的2倍并不严谨。

  ```java
      /**
       * Constructs an empty vector so that its internal data array
       * has size {@code 10} and its standard capacity increment is
       * zero.
       * 初始化容量是10，capacity increment 数组容量的增量是0。
       */
      public Vector() {
          this(10);
      }
  
      private void grow(int minCapacity) {
          // overflow-conscious code
          int oldCapacity = elementData.length;
          //如果是通过不带参数的构造方法构造的Vector，capacityIncrement=0
          int newCapacity = oldCapacity + ((capacityIncrement > 0) ?
                                           capacityIncrement : oldCapacity);
          if (newCapacity - minCapacity < 0)
              newCapacity = minCapacity;
          if (newCapacity - MAX_ARRAY_SIZE > 0)
              newCapacity = hugeCapacity(minCapacity);
          elementData = Arrays.copyOf(elementData, newCapacity);
      }
  ```

##### LinkedList

ArrayList和Vector底层结构是数组，LinkedList是双向链表，其主要特点是

* 有序
* 查询相对慢，新增相对快
* 非线程安全

### 集合排序

集合实现排序除了本身具有顺序（如LinkedList）的集合外，还有辅助工具可以实现集合排序。

#####  Comparable & Comparator

java集合要实现自定义排序的2个途径

1. 一般要实现`java.lang.Comparable`接口，重写compareTo(o)方法。适用于本实例和其他对象比较，内置比较/排序功能。支持排序的集合中的元素必须实现它。
2. 使用`java.util.Comparator`接口，既然在util包里，本质是一个工具类，里面提供了很多排序的方法。当集合中元素没实现Comparable接口或者Comparable接口不能满足需求时，可以用Comparator实现排序。

##### 排序工具

* Collections

  通过Collections进行自动排序，正序、倒序、乱序。

```java
Collections#
//正序 实现Comparable接口的排序
public static <T extends Comparable<? super T>> void sort(List<T> list) {
        list.sort(null);
    }
//使用Comparator实现排序
public static <T> void sort(List<T> list, Comparator<? super T> c) {
        list.sort(c);
}    
//倒序
public static void reverse(List<?> list){...}
//乱序,每次执行的顺序不一样
public static void shuffle(List<?> list){...}
```

* java8 Stream 排序

```java
List<User> list = new ArrayList();
list.stream().sorted(Comparator.comparing(User::getName));
```

* list.sort

```java
List<User> list = new ArrayList();
list.sort(Comparator.comparing(User::getName));
//注意list.sort是interface List的方法实现。java8开始运行接口方法可以有方法体。
```

* Arrays

  ```java
  //Arrays排序
  public static <T> void sort(T[] a, Comparator<? super T> c){...}
  ```

### 集合里为什么设计出迭代器

集合遍历是比较常用的操作，每类集合遍历的方式各不相同，list和set的遍历显然不同，但它们又都属于集合。有没有一种不关心具体集合类型就可以遍历的方式呢？答案就是迭代器。只要实现了Iterator接口就可以用统一的遍历方式。总结一下用Iterator的优点

1. 不了解集合内部结构也可以遍历
2. 适用性强，实现了Iterator接口的类都可以用统一的遍历方式
3. 符合开闭原则，当集合类型变更时不需要重写遍历方式
4. Iterator的remove方法是安全的，在上文的Arraylist#remove有详细说明。

### 总结

本文从集合的源码级别分析了它们各自特点和彼此之间的区别。个人能力有限，水平一般，难免会有一些谬误，还请各位体谅。原创不易，欢迎点赞转发。