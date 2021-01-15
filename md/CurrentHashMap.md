# 史上最全ConcurrentHashMap源码解析，含6大核心方法

本文是建立在上篇[HashMap源码分析](http://mp.weixin.qq.com/s?__biz=MzIwNDc4Nzg3OA==&mid=100000055&idx=1&sn=2b73c786bd5f4e97c3a6d396c9f7ef8a&chksm=173b98fe204c11e8f3a37ef693f97f98b28f357ac371169b2ede1a8e49f616484ed01d0fe276#rd)的基础上。其中的一些重复的方法和知识点不会再赘述。有疑惑的同学可以移步到上一篇文章。依旧以jdk1.8源码为基础来讲解ConcurrentHashMap。它的大体结构与HashMap相同，table容量同样要求是2的幂次。

HashMap高效快捷，但不安全，特别是2020年，安全很重要。目前市面上有3种提供安全的map的方式，分别是

1. hashtable：相对古老的线程安全机制。任一时间只有一个线程能写操作。现在基本弃用，被效率更高的ConcurrentHashMap替代。
2. synchronizedMap：Collections中的内部类，可以把普通的map转成线程安全的map。原理就是在操作对象上加synchronized。
3. ConcurrentHashMap：线程安全的HashMap。

## 如何做到线程安全

多线程并发带来的问题目前总体有2种解决机制。

1. **悲观机制**：认为最坏的结果肯定发生，从开始就设置一定规则最大限度减少发生概率，有点以防万一、未雨绸缪的意思。比如安全套，肯定不会每次都中，可万一呢？并发的悲观实现一般采用锁，java中的synchronized关键字也被广泛应用在多线程并发的场景中。但是锁会降低程序性能。
2. **乐观机制**：认为结果总是好的，先干了再说，不行再想办法：重试，补救，版本号控制等。意外怀孕的痴男怨女们可能就太乐观了，事后只能采取补救措施。CAS就是乐观机制。

ConcurrentHashMap中主要采用的CAS+自旋，改成功就改，改不成功继续试（自旋）。也有synchronized配合使用。

### CAS & Unsafe

CAS的全称是Compare And Swap，即比较交换，它也是JUC的基础。我们只是简单介绍它的原理，细节问题需要同学们另行研究。

```java
	/*
   * v-表示要更新的变量,e-表示预期值,n-新值。
   * 方法的目的是给变量v修改值。
   * 如果变量v的值和预期值e相等就把v的值改成n，返回true，如果不相等就返回false，有其他线程修改该值。
   * 这里可能出现ABA问题（从A变成B又变回A，造成变量没变得假象），但java做了很多优化。可以忽略不计。
  */
  boolean CAS(v,e,n) 
```

cas流程很好理解，但在多cpu多线程的情况下会不会不安全，放心安全。java的cas其实是通过Unsafe类方法调用cpu的一条cas原子指令。操作系统本身是对内存操作做了线程安全的，篇幅太少也说不清楚，这里大家可以自行研究一下JMM，JMM不是本文重点。这里只要知道结论就是CAS可以保证原子性。不过它只提供单个变量的原子性，多个变量的原子性还需要借助synchronized。

Unsafe是java里的另类，java有个特点是安全，并不允许程序员直接操作内存，而Unsafe类可以，才有条件执行CAS方法。但是不建议大家使用Unsafe.class，因为不安全，sun公司有计划取消Unsafe。

## 源码解析

### sizeCtl & constructor

ConcurrentHashMap和HashMap在各自的构造函数中都没有做数组初始化，初始化放在了第一次添加元素中。值得注意的是ConcurrentHashMap中有一个属性**sizeCtl**特别重要，理清楚它的变化，也就理解了整个Map源码的流程。下面是它的说明

```java
   
		/**
     * Table initialization and resizing control.  When negative, the
     * table is being initialized or resized: -1 for initialization,
     * else -(1 + the number of active resizing threads).  Otherwise,
     * when table is null, holds the initial table size to use upon
     * creation, or 0 for default. After initialization, holds the
     * next element count value upon which to resize the table.
     * <p>
     * 控制标识符，用来控制table的初始化和扩容的操作，不同的值有不同的含义
     * <p>
     * 1. 当为负数时：-1代表正在初始化，-N代表有N-1个线程正在进行扩容
     * <p>
     * 2.当为0时：代表当时的table还没有被初始化
     * <p>
     * 3.当为正数时：未初始化表示的是初始化数组的初始容量，如果已经初始化，
     * 记录的是扩容的阈值（达到阈值进行扩容）
     */
    private transient volatile int sizeCtl;

```

再看一下ConcurrentHashMap带初始化容量的代码

```java
   /**
     * Creates a new, empty map with an initial table size
     * accommodating the specified number of elements without the need
     * to dynamically resize.
     *
     * @param initialCapacity The implementation performs internal
     * sizing to accommodate this many elements.
     * @throws IllegalArgumentException if the initial capacity of
     * elements is negative
     *
     * 此时sizeCtl记录的就是数组的初始化容量
     * 
     * 比如initialCapacity=5
     * 调用tableSizeFor（5+5/2+1）==tableSizeFor(8)
     */
    public ConcurrentHashMap(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException();
        int cap = ((initialCapacity >= (MAXIMUM_CAPACITY >>> 1)) ?
                   MAXIMUM_CAPACITY :
                   tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1));
        this.sizeCtl = cap;
    }
    
    /**
     * Returns a power of two table size for the given desired capacity.
     * See Hackers Delight, sec 3.2
     * 返回一个大于等于c的2的幂次方数
     *  
     * 当c=8时
     * n = c-1=7
     * 接下来验算最终结果
     * 0000 0000 0000 0000 0000 0000 0000 0111
     * >>> 1
     * = 0000 0000 0000 0000 0000 0000 0000 0011
     * | 0000 0000 0000 0000 0000 0000 0000 0111
     * = 0000 0000 0000 0000 0000 0000 0000 0111
     *  >>> 2
     * = 0000 0000 0000 0000 0000 0000 0000 0001
     * | 0000 0000 0000 0000 0000 0000 0000 0111
     * = 0000 0000 0000 0000 0000 0000 0000 0111
     *  >>> 4
     * = 0000 0000 0000 0000 0000 0000 0000 0000
     * | 0000 0000 0000 0000 0000 0000 0000 0111
     * = 0000 0000 0000 0000 0000 0000 0000 0111
     * 下面再 >>> 8 和 >>> 16后的二进制都是0
     * 所以最终结果就是111，也就是7最后返回结果再+1，等于8
     * 
     * 总结 右移一共1+2+4+8+16=31位，和与之对应 | 运算
     * 最终把n的二进制中所有1移到低位。新的数高位都是0，低位都是1。这样格式的数在HashMap中提到过，就是2的幂次-1。
     * 最后结果是这个数+1，那就是2的幂次。
     */
    private static final int tableSizeFor(int c) {
        int n = c - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
```

当我们`new ConcurrentHashMap(c)` 时，初始化容量并不是c，而是一个大于等于c的2的幂次方数。我们利用发射来验证下

```java
public static void main(String[] args) {
        ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap(5);
        Class clazz = concurrentHashMap.getClass();
        try {
            Field field = clazz.getDeclaredField("sizeCtl");
            //打开私有访问
            field.setAccessible(true);
            //获取属性
            String name = field.getName();
            //获取属性值
            Object value = field.get(concurrentHashMap);
            System.out.println("ConcurrentHashMap的初始容量为：= "+value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
--打印结果是: Map的初始容量=8

```

### put & putVal

```java
		/**
     * Maps the specified key to the specified value in this table.
     * Neither the key nor the value can be null.
     *
     * <p>The value can be retrieved by calling the {@code get} method
     * with a key that is equal to the original key.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with {@code key}, or
     * {@code null} if there was no mapping for {@code key}
     * @throws NullPointerException if the specified key or value is null
     */
    @Override
    public V put(K key, V value) {
        return putVal(key, value, false);
    }

    /**
     * Implementation for put and putIfAbsent
     *
     */
    final V putVal(K key, V value, boolean onlyIfAbsent) {
        //如果有空值或者空键，直接抛异常
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        //两次hash，减少hash冲突，可以均匀分布
        int hash = spread(key.hashCode());
        int binCount = 0;
        //迭代当前table
        for (Node<K, V>[] tab = table; ; ) {
            Node<K, V> f;
            int n, i, fh;
            //1. 如果table未初始化，先初始化
            if (tab == null || (n = tab.length) == 0) {
                tab = initTable();
            }
            //如果i位置没有数据，cas插入
            else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
                //cas和外侧else if条件形成双保险，保证数据安全
                if (casTabAt(tab, i, null,
                        new Node<K, V>(hash, key, value, null))) {
                    break;  // no lock when adding to empty bin
                }
            }
            //2. hash值是MOVED表示数组正在扩容，则协助扩容，先扩容在新加元素
            else if ((fh = f.hash) == MOVED) {
                tab = helpTransfer(tab, f);
            } else {
                //hash计算的bucket不为空，且当前没有处于扩容操作，进行元素添加
                V oldVal = null;
                //对当前bucket进行加锁，保证线程安全，执行元素添加操作
                synchronized (f) {
                    //判断是否为f，防止它变成tree
                    if (tabAt(tab, i) == f) {
                        //hash值>=0 表示该节点是链表结构
                        if (fh >= 0) {
                            binCount = 1;
                            //e记录的是头节点
                            for (Node<K, V> e = f; ; ++binCount) {
                                K ek;
                                //相同的key进行put就会覆盖原先的value
                                if (e.hash == hash &&
                                        ((ek = e.key) == key ||
                                                (ek != null && key.equals(ek)))) {
                                    oldVal = e.val;
                                    if (!onlyIfAbsent)
                                        e.val = value;
                                    break;
                                }
                                Node<K, V> pred = e;
                                if ((e = e.next) == null) {
                                    //插入链表尾部
                                    pred.next = new Node<K, V>(hash, key,
                                            value, null);
                                    break;
                                }
                            }
                        } else if (f instanceof TreeBin) {
                            Node<K, V> p;
                            binCount = 2;
                            //红黑树结构旋转插入
                            if ((p = ((TreeBin<K, V>) f).putTreeVal(hash, key,
                                    value)) != null) {
                                oldVal = p.val;
                                if (!onlyIfAbsent) {
                                    p.val = value;
                                }
                            }
                        }
                    }
                }
                if (binCount != 0) {
                    //链表长度大于8时转换红黑树
                    if (binCount >= TREEIFY_THRESHOLD) {
                        treeifyBin(tab, i);
                    }
                    if (oldVal != null) {
                        return oldVal;
                    }
                    break;
                }
            }
        }
        //统计size，并且检查是否需要扩容
        addCount(1L, binCount); 
        return null;
    }
```

`putVal()`总体是自旋+CAS的方式，流程和HashMap一样。

* 自旋：
  1. 如果table==null，调用initTable()初始化
  2. 如果没有hash碰撞就CAS添加
  3. 如果正在扩容就协助扩容
  4. 如果存在hash碰撞，如果是单向列表就插到bucket尾部，如果是红黑树就插入数结构
  5. 如果链表bucket长度大于8，转红黑树
  6. 如果添加成功就调用addCount()方法统计size，检查是否需要扩容

从源码中可以看到put新元素时，如果发生hash冲突，先锁定发生冲突的bucket，不影响其他bucket操作，达到并发安全且高效的目的。下面是`putVal

### initTable，初始化table

```java
   /**
     * Initializes table, using the size recorded in sizeCtl.
     * 初始化table，从新记录sizeCtl值，此时值为数组下次扩容的阈值
     */
    private final Node<K, V>[] initTable() {
        Node<K, V>[] tab;
        int sc;
        //再次判断空的table才能进入初始化操作
        while ((tab = table) == null || tab.length == 0) {
            // sizeCtl<0，也就是下面elseif把sizeCtl设置成-1. 表示其他线程已经在初始化了或者扩容了，挂起当前线程，自旋等待
            if ((sc = sizeCtl) < 0) {
                Thread.yield(); 
             //CAS设置SIZECTL为-1，如果设置成功继续执行下面操作，如果失败，说明此时有其他线程正在执行操作，继续自旋
            } else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                try {
                    //double check，保证线程安全，可能有线程已经同步完了
                    if ((tab = table) == null || tab.length == 0) {
                        int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                        Node<K, V>[] nt = (Node<K, V>[]) new Node<?, ?>[n];
                        table = tab = nt;
                        //记录下次扩容的大小，相当于n-n/4=0.75n
                        sc = n - (n >>> 2); 
                    }
                } finally {
                    //此时sizeCtl的值为下次扩容的阈值
                    sizeCtl = sc;
                }
                break;
            }
        }
        return tab;
    }
```

### helpTransfer 协助扩容

```java
    /**
     * Helps transfer if a resize is in progress.
     * <p>
     * 如果数组正在扩容，协助之，多个工作线程一起扩容
     * 从旧的table的元素复制到新的table中
     *
     */
    final Node<K, V>[] helpTransfer(Node<K, V>[] tab, Node<K, V> f) {
        Node<K, V>[] nextTab;
        int sc;
        //如果f是ForwardingNode，说明f正在扩容，hash值已经被标为MOVED。
        //ForwardingNode.nextTable就是新table不为空
        if (tab != null && (f instanceof ForwardingNode) &&
                (nextTab = ((ForwardingNode<K, V>) f).nextTable) != null) {
            //根据 length 得到一个前16位的标识符，数组容量大小。
            int rs = resizeStamp(tab.length);
            //多重条件判断未扩容完成，还在进行中,新老数组都没有变，且sizeCtl<0
            while (nextTab == nextTable && table == tab &&
                    (sc = sizeCtl) < 0) {
            // 1. sizeCtl 无符号右移16位获得高16位如果不等 rs 标识符变了
            // 2. (sc == rs + 1),表示扩容结束 
            // 3. （sc == rs + MAX_RESIZERS）达到了最大帮助线程个数 65535个
            // 4. transferIndex<= 0 也表示扩容已经结束
                if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                        sc == rs + MAX_RESIZERS || transferIndex <= 0)
                    break;
                //增加一个线程帮助扩容
                if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {
                    transfer(tab, nextTab);
                    break;
                }
            }
            return nextTab;
        }
        return table;
    }
```

### TreeNode结构

```java
    /**
     * Nodes for use in TreeBins
     */
    static final class TreeNode<K, V> extends Node<K, V> {
        TreeNode<K, V> parent;  // red-black tree links
        TreeNode<K, V> left;
        TreeNode<K, V> right;
        TreeNode<K, V> prev;    // needed to unlink next upon deletion
        boolean red;

        TreeNode(int hash, K key, V val, Node<K, V> next,
                 TreeNode<K, V> parent) {
            super(hash, key, val, next);
            this.parent = parent;
        }
    }
```

**TreeNode继承了Node，又多了prev等，这里很少人注意，其实它在维护红黑树的同时也维护了双向列表。虽然红黑树查询方便，但迁移真的好难，借助双向列表做迁移会容易很多。**

### transfer 单向列表扩容

```java
		/**
     * Moves and/or copies the nodes in each bin to new table. See
     * above for explanation.
     * 多线程扩容操作
     */
    private final void transfer(Node<K, V>[] tab, Node<K, V>[] nextTab) {
        int n = tab.length, stride;
        //数组迁移分块执行，每核处理的bucket量小于16个，则强制赋值16，
        if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE) {
            stride = MIN_TRANSFER_STRIDE; // subdivide range
        }
        //如果是扩容线程，此时新数组为null
        if (nextTab == null) {            // initiating
            try {
                //构建新数组，其容量为原来容量的2倍
                @SuppressWarnings("unchecked")
                Node<K, V>[] nt = (Node<K, V>[]) new Node<?, ?>[n << 1];
                nextTab = nt;
            } catch (Throwable ex) {      // try to cope with OOME
                sizeCtl = Integer.MAX_VALUE;
                return;
            }
            nextTable = nextTab;
            //记录线程开始迁移的bucket，从后往前迁移
            transferIndex = n;
        }
        int nextn = nextTab.length;
    		//已经迁移的桶位，会用fwd占位（这个节点的hash值为MOVED），这个在put方法中见到过
        ForwardingNode<K, V> fwd = new ForwardingNode<K, V>(nextTab);
        // 当advance == true时，表明该节点已经处理过了
        boolean advance = true;
        boolean finishing = false; // to ensure sweep before committing nextTab
        for (int i = 0, bound = 0; ; ) {
            Node<K, V> f;
            int fh;
            //计算每一个线程负责哪部分，迁移以后赋fwd节点          
            //i记录当前正在迁移桶位的索引值
            //bound记录下一次任务迁移的开始桶位
            //--i>=bound 表示当前线程分配的迁移任务还没有完成
            while (advance) {
                int nextIndex, nextBound;
                if (--i >= bound || finishing) {
                    advance = false;
                //没有元素需要迁移
                } else if ((nextIndex = transferIndex) <= 0) {
                    i = -1;
                    advance = false;
                } else if (U.compareAndSwapInt // 用CAS计算得到下一次任务迁移的开始桶位，值值给transferIndex
                        (this, TRANSFERINDEX, nextIndex,
                                nextBound = (nextIndex > stride ?
                                        nextIndex - stride : 0))) {
                    bound = nextBound;
                    i = nextIndex - 1;
                    advance = false;
                }
            }
            //没有更多的需要迁移的bucket
            if (i < 0 || i >= n || i + n >= nextn) {
                int sc;
                // 扩容结束后，table指向新数组，重新计算扩容阈值，赋值给sizeCtl
                if (finishing) {
                    nextTable = null;
                    table = nextTab;
                    sizeCtl = (n << 1) - (n >>> 1);
                    return;
                }
              	// 扩容任务线程数减1
                if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
                    //判断当前所有扩容任务是否执行完成,相等表明完成
                    if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT) {
                        return;
                    }
                    finishing = advance = true;
                    i = n; // recheck before commit
                }
            } else if ((f = tabAt(tab, i)) == null) { //当前节点为null，在该位置添加一个ForwardingNode
                advance = casTabAt(tab, i, null, fwd);
            } else if ((fh = f.hash) == MOVED) {//如果是ForwardingNode，说明已经扩容过
                advance = true; // already processed
            } else {
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        Node<K, V> ln, hn;
                        // fh >= 0 ,表示为链表节点
                        if (fh >= 0) {
                            // 构造两个链表，一个是原链表，另一个是原链表的反序排列
                            int runBit = fh & n;
                            Node<K, V> lastRun = f;
                            for (Node<K, V> p = f.next; p != null; p = p.next) {
                                int b = p.hash & n;
                                if (b != runBit) {
                                    runBit = b;
                                    lastRun = p;
                                }
                            }
                            if (runBit == 0) {
                                ln = lastRun;
                                hn = null;
                            } else {
                                hn = lastRun;
                                ln = null;
                            }
                            for (Node<K, V> p = f; p != lastRun; p = p.next) {
                                int ph = p.hash;
                                K pk = p.key;
                                V pv = p.val;
                                if ((ph & n) == 0) {
                                    ln = new Node<K, V>(ph, pk, pv, ln);
                                } else {
                                    hn = new Node<K, V>(ph, pk, pv, hn);
                                }
                            }
                            // 先扩容再插入相应值
                            //新table的i位置添加元素
                            setTabAt(nextTab, i, ln);
                            //新table的i+1位置添加元素
                            setTabAt(nextTab, i + n, hn);
                            // 旧table i 位置处插上ForwardingNode，表示该节点已经处理过
                            setTabAt(tab, i, fwd);
                            advance = true;
                            // 红黑树处理逻辑，实质上是维护双向链表
                        } else if (f instanceof TreeBin) {
                            TreeBin<K, V> t = (TreeBin<K, V>) f;
                            TreeNode<K, V> lo = null, loTail = null;
                            TreeNode<K, V> hi = null, hiTail = null;
                            int lc = 0, hc = 0;
                            for (Node<K, V> e = t.first; e != null; e = e.next) {
                                int h = e.hash;
                                TreeNode<K, V> p = new TreeNode<K, V>
                                        (h, e.key, e.val, null, null);
                                if ((h & n) == 0) {
                                    if ((p.prev = loTail) == null) {
                                        lo = p;
                                    } else {
                                        loTail.next = p;
                                    }
                                    loTail = p;
                                    ++lc;
                                } else {
                                    if ((p.prev = hiTail) == null) {
                                        hi = p;
                                    } else {
                                        hiTail.next = p;
                                    }
                                    hiTail = p;
                                    ++hc;
                                }
                            }
                            // 扩容后红黑树节点个数若<=6，将树转单向链表
                            ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :
                                    (hc != 0) ? new TreeBin<K, V>(lo) : t;
                            hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) :
                                    (lc != 0) ? new TreeBin<K, V>(hi) : t;
                            setTabAt(nextTab, i, ln);
                            setTabAt(nextTab, i + n, hn);
                            setTabAt(tab, i, fwd);
                            advance = true;
                        }
                    }
                }
            }
        }
    }

```

## 总结

ConcurrentHashMap设计之精妙惊为天人，不愧为大师之作。有3个点可能之前没有注意

1. 在`new ConcurrentHashMap(c)`时，初始容量并不是传入的值。而是一个大于等于该值的2的幂次方值
2. 世人都知道链表大于6的时候会转红黑树，却很少有人提及在红黑树节点个数小于等于6时会转成链表
3. ConcurrentHashMap和HashMap的数据结构严格的说应该是**数组+单向列表+（红黑树+双向链表）**

本人水平有限，文中难免会有谬误，还请各位指出。

## 参考

jdk1.8 & jdk1.7#ConcurrentHashMap源码

黑马程序员公开课