# ThreadLocal原理及使用场景大揭秘

## 是什么

ThreadLocal从名字上看好像是一个Thread，其实并不是，它是Thread的局部变量的维护类。作用是让变量私有化（为每个Thread提供变量的副本），以此来实现线程间变量的隔离。比如有一个变量`count`，在多线程并发时操作`count++`会出现线程安全问题。但是通过ThreadLocal<Integer> count，就可以为每个线程创建只属于当前线程的count副本，各自操作各自的副本，不会影响到其他线程。我们先有个概念，具体还是看源码（JDK1.8）。

## 原理源码

简单用法

```java
public static void main(String[] args) {
        ThreadLocal<String> a = new ThreadLocal<String>();
        a.set("1");
        a.set("2");
        System.out.println(a.get());
    }
//输出结果是2。貌似“1”被覆盖了。
```

先看一下set(T value)方法。

```java
  /**
     * Sets the current thread's copy of this thread-local variable
     * to the specified value.  Most subclasses will have no need to
     * override this method, relying solely on the {@link #initialValue}
     * method to set the values of thread-locals.
     *
     * @param value the value to be stored in the current thread's copy of
     *        this thread-local.
     *
     */
    public void set(T value) {
        //当前线程
        Thread t = Thread.currentThread();
        //获取ThreadLocalMap
        ThreadLocalMap map = getMap(t);
        //map为空就创建，不为空就set
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
    }
     //给t.threadLocals赋值成ThreadLocalMap实例。
    void createMap(Thread t, T firstValue) {
        t.threadLocals = new ThreadLocalMap(this, firstValue);
    }

    ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
            table = new Entry[INITIAL_CAPACITY];
            int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
            table[i] = new Entry(firstKey, firstValue);
            size = 1;
            setThreshold(INITIAL_CAPACITY);
   }
   /**
   * Set the resize threshold to maintain at worst a 2/3 load factor.
   */
   private void setThreshold(int len) {
       threshold = len * 2 / 3;
   }
   
   /* 当前线程对于的ThreadLocalMap实例，在ThreadL类中*/
   ThreadLocal.ThreadLocalMap threadLocals = null;
```

这段代码逻辑比较简单，主要看ThreadLocalMap，它是TreadLocal的内部类，虽然没有实现Map接口，但看它的几个主要属性：`Entry[] table、size、threshold、INITIAL_CAPACITY`，和`java.util.HashMap`极其类似。关于这些属性更详尽的解释可以看一下这篇[深入讲解HashMap的工作原理](https://mp.weixin.qq.com/s?__biz=MzIwNDc4Nzg3OA==&mid=2247483704&idx=1&sn=f2752a02b84f63d3c30e1c62ffdea607&chksm=973b98f1a04c11e71063ec6bb8bede27f1665f93bbb063184c9f2fb082f8c7f3b128a8b00fdb&token=430534171&lang=zh_CN#rd) 。class注释中也提到它是一个为存放本地线程值而定制的hash map。它的key就是ThreadLocal当前实例this，值就是set的参数值。既然是hash map，就有可能出现hash冲突的问题，再复习一下解决hash冲突的常见方法

1. **再哈希法**：如果hash出的index已经有值，就再hash，不行继续hash，直至找到空的index位置。

2. **开放地址法**：如果hash出的index已经有值，通过算法在它前面或后面的若干位置寻找空位。

3. **建立公共溢出区：** 把冲突的hash值放到另外一块溢出区。

4. **链式地址法：** 把产生hash冲突的hash值以链表形式存储在index位置上。HashMap的解决方案。

ThreadLocalMap用的**是开放地址方法**，如果当前位置有值，就继续寻找下一个位置，注意table[len-1]的下一个位置是table[0]，就像是一个环形数组，所以也叫**闭散列**法。如果一直都找不到空位置就会出现死循环，发生内存溢出。当然有扩容机制，一般不会找不到空位置的。

```java
/**
* ThreadLocalMap is a customized hash map suitable only for
* maintaining thread local values. No operations are exported
* outside of the ThreadLocal class. The class is package private to
* allow declaration of fields in class Thread.  To help deal with
* very large and long-lived usages, the hash table entries use
* WeakReferences for keys. However, since reference queues are not
* used, stale entries are guaranteed to be removed only when
* the table starts running out of space.
* 
*/
static class ThreadLocalMap {
    
   static class Entry extends WeakReference<ThreadLocal<?>> {
            /** The value associated with this ThreadLocal. */
            Object value;
      Entry(ThreadLocal<?> k, Object v) {
          super(k);
          value = v;
        }
   }
   
  /**
   * The table, resized as necessary.
   * table.length MUST always be a power of two.
   * table.length必须是2的幂次
   */
   private Entry[] table;

  /**
   * The number of entries in the table.
   * table实际已经存放#Entry的数量
  */
    private int size = 0;

   /**
    * The next size value at which to resize.
    * table扩容的阈值，初始threshold = length * 2 / 3，当size>threshold*3/4时就扩容
    */
    private int threshold;
  
    /**
    * The initial capacity -- MUST be a power of two.
    * table 的默认容量
    */
    private static final int INITIAL_CAPACITY = 16;
   
   /**
    * Set the value associated with key.
    *
    * @param key the thread local object
    * @param value the value to be set
    */
    private void set(ThreadLocal<?> key, Object value) {
            Entry[] tab = table;
            int len = tab.length;
            //计算key的角标index。就是用key的threadLocalHashCode & (len-1)等效于key.threadLocalHashCode%len
            //只是&要比%效率高，它们之所以可以等效，因为len是2的n次幂。
            //threadLocalHashCode并不影响读懂这块代码，放在后面详说
            int i = key.threadLocalHashCode & (len-1);
            //开放地址方法，循环tab
            for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                ThreadLocal<?> k = e.get();
                //key相同，更新value
                if (k == key) {
                    e.value = value;
                    return;
                }
                //key为空，说明ThreadLocal实例被回收了，用新key-value替代
                if (k == null) {
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }
            //table[i]=null 新建一个Entity，++size
            tab[i] = new Entry(key, value);
            int sz = ++size;
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
                rehash();
        }
    //整理table
   private void rehash() {
      //删除table[]陈旧元素
      expungeStaleEntries();
      //size依然大于3/4 threshold，扩容
      if (size >= threshold - threshold / 4)
          resize();
    }
/**
 * Expunge all stale entries in the table.
 * 删除table[]所有key==null的entity
*/
	 private void expungeStaleEntries() {
    Entry[] tab = table;
     int len = tab.length;
      for (int j = 0; j < len; j++) {
       Entry e = tab[j];
        if (e != null && e.get() == null)
           expungeStaleEntry(j);
        }
  }
   
   /**
     * Double the capacity of the table.
     * 扩容为原数组的2倍
    */
   private void resize() {
            Entry[] oldTab = table;
            int oldLen = oldTab.length;
            int newLen = oldLen * 2;
            //创建2倍容量的新数组
            Entry[] newTab = new Entry[newLen];
            int count = 0;

            for (int j = 0; j < oldLen; ++j) {
                Entry e = oldTab[j];
                if (e != null) {
                    //如果线程的
                    ThreadLocal<?> k = e.get();
                    if (k == null) {
                        e.value = null; // Help the GC
                    } else { 
                        //计算新数组index
                        int h = k.threadLocalHashCode & (newLen - 1);
                        while (newTab[h] != null)
                            h = nextIndex(h, newLen);
                        newTab[h] = e;
                        count++;
                    }
                }
            }

            setThreshold(newLen);
            size = count;
            table = newTab;
   }
   //返回当前线程对应的ThreadLocalMap
   ThreadLocalMap getMap(Thread t) {
        return t.threadLocals;
   }
}
```

看了set方法，get方法就

```java
/**
     * Returns the value in the current thread's copy of this
     * thread-local variable.  If the variable has no value for the
     * current thread, it is first initialized to the value returned
     * by an invocation of the {@link #initialValue} method.
     *
     * @return the current thread's value of this thread-local
*/
public T get() {
  Thread t = Thread.currentThread();
  ThreadLocalMap map = getMap(t);
  if (map != null) {
       ThreadLocalMap.Entry e = map.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
             T result = (T)e.value;
              return result;
            }
        }
    return setInitialValue();
}
/**
     * Variant of set() to establish initialValue. Used instead
     * of set() in case user has overridden the set() method.
     *
     * @return the initial value
  */
private T setInitialValue() {
   T value = initialValue();//null
   Thread t = Thread.currentThread();
   ThreadLocalMap map = getMap(t);
   if (map != null)
        map.set(this, value);
   else
       createMap(t, value);
        return value;
 }
//默认值null
protected T initialValue() {
  return null;
}
```

### 源码总结：

总体来讲，ThreadLocal源码比较好理解。ThreadLocalMap虽然在ThreadLocal中定义，但是被Thread.threadLocals引用。这样保证了一个Thread拥有独立的ThreadLocalMap，做到和其他线程隔离。而ThreadLocalMap的key就是ThreadLocal实例，value就是线程变量。

再看一下最开始的源码。

```java
public static void main(String[] args) {
        ThreadLocal<String> a =new ThreadLocal<String>();
        a.set("1");
        a.set("2");
        System.out.println(a.get());
    }
//输出结果是2。貌似“1”被覆盖了。
//确实是被覆盖了，Thread.threadLocals的key是a，值当然只能有一个，get到的值也是最后一个value
//单线程的内部实现类似这样
   ThreadLocal<String> a =new ThreadLocal<String>();
   Map map = new HashMap();
   map.put(a,"1");
   map.put(a,"2");
   System.out.println(map.get(a));
```

### 源码中的问题总结

1. ThreadLocalMap的hash冲突问题

上文说到ThreadLocalMap解决hash冲突的方法是开放地址。但对threadLocalHashCode没有详细说明，下面补充说明一下它。

```java
//计算数组下标
int i = key.threadLocalHashCode & (len-1);

private final int threadLocalHashCode = nextHashCode();
/**
* The next hash code to be given out. Updated atomically. Starts at
* zero.
* 线程安全的原子类，发出下一个hash code
*/
private static AtomicInteger nextHashCode = new AtomicInteger();
/**
* getAndAdd(v)返回的结果是nextHashCode，但是nextHashCode+=HASH_INCREMENT；
*/
private static int nextHashCode() {
  return nextHashCode.getAndAdd(HASH_INCREMENT);
}

/**
* The difference between successively generated hash codes - turns
* implicit sequential thread-local IDs into near-optimally spread
* multiplicative hash values for power-of-two-sized tables.
* 自增量
*/
private static final int HASH_INCREMENT = 0x61c88647;
```

因为`nextHashCode`被`static`修饰，所以每次`new ThreadLocal()`都会自增`HASH_INCREMENT`，其值和斐波那契散列（Fibonacci）有关，主要目的是为了让哈希码能均匀的分布在2的n次方的数组里。这也是为什么table的容量是2的n次方的一个原因。

2. 内存泄漏 & 弱引用

   ThreadLocal使用不当可能会出现**内存泄露**，进而可能导致内存溢出**，

   1. **内存泄露（Memory Leak）**：垃圾对象没有及时回收或无法回收，一般情况下是因为对象有错误的引用，导致内存浪费，这些垃圾越来越多可能会导致**内存溢出**，
   2. **内存溢出（OOM）**：没有足够的内存提供申请者使用。

   当然了，任何操作不当都会出现内存泄露或其他bug，我们这里只谈论ThreadLocal。

   回顾Thread、ThreadLocal、ThreadLocalMap的关系。

   1. Thread.threadLocals引用ThreadLocalMap，生命周期一致。
   2. ThreadLocal定义ThreadLocalMap
   3. ThreadLocalMap#Entry弱引用ThreadLocal。我们通常说一个对象不被引用就会被gc回收，其实说的是强引用。但弱引用对象是，GC时，不管有没有被引用都会被垃圾回收。

   当一个Thread执行完，被销毁后，Thread.threadLocals指向的ThreadLocalMap实例也会随之变为垃圾，当然它里面存放的Entity也会被回收。这时是不会发生内存泄漏的。

   发生内存泄漏一般是在线程池，Thread生命周期比较长，threadLocals引用一直存在，当其存放的ThreadLocal被回收（弱引用生命周期比较短）后，它对应的Entity就成了key==null的实例，依然不会被回收。如果此Entity一直不被`get()、set()、remove()`它就一直不会被回收，也就发生了内存泄漏。通常在使用完ThreadLocal都会调用它的`remove()`。

   补充：在ThreadLocal的get、set的时候，都会检查当前Entity的key是否为null，如果是null就把Entity释放掉，被垃圾回收。

## 应用场景

它的应用场景主要有

1. 线程安全，包裹线程不安全的工具类，比如`java.text.SimpleDateFormat`类，当然jdk1.8已经给出了对应的线程安全的类`java.time.format.DateTimeFormatter`
2. 线程隔离，比如数据库连接管理、申明式事物、Session管理、mdc日志追踪等。

最近在与前端对接的接口中用到了ThreadLocal。大概流程是，前端在请求后端接口时在header带上toekn，拦截器通过token获取到用户信息，通过ThreadLocal保存。主要代码如下：

```java
//接口请求时先走filter
public boolean checkUserLogin(String token){
   UserDTO user = getUserByToken(token);
   ContextUtil.setUserId(user.getId());
}

public class ContextUtil {
    private static ThreadLocal<String> userIdHolder = new ThreadLocal();
    //存储userid
    public static void setUserId(String userId) {
        userIdHolder.set(userId);
    }
    
    public static String getUserId() {
        return (String)userIdHolder.get();
    }
}    

//实际调用接口
void invokeInterface(){
   String userId = ContextUtil.getUserId();
   .....
}
```

每一次接口请求都是一个线程，在校验接口合法后把userid存入ThreadLocal，以备后续之用。

## 总结

我们通过源码，对ThreadLocal的原理和应用作了深入讲解。当然本人能力一般，水平有限，难免有些谬误。还请各位多担待，欢迎指正。有反馈才有成长。