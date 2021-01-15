# 搞定HashMap面试，深入讲解HashMap的工作原理

> 摘要：HashMap是近几年java面试新秀，出场率高达80%以上，如此高频的出场不得不让码农们慎重其事。但依旧拜倒在它的石榴裙下，让面试场面一度尴尬。它也是开发中最常用到的key-value数据类型。

为什么HashMap深受面试官青睐，我想有这3个原因：

1. 常用、基础
2. 线程不安全，容易出问题
3. 大厂都在问，不问显得面试官没水平

Hash虐我千百遍，我视它为初恋，真的是又爱又恨。爱的是每次面试都有它，可以提前做准备，恨的是准备也白准备，依然被灭。

这次要做回真正的男人，和它做一个了断。互虐一次，一劳永逸。

我们以天（小）使（白）视角来解剖一下HahsMap。

#### 首先要明白几个概念

* hash值

> 是把任意长度的输入通过散列算法变换成固定长度的输出，该输出就是hash值。java中，hash值就是一个固定长度的int值。补充一点，java的int 4字节，32位。

* hash表

> 存储hash值的数组就是hash表。

* hash函数

> hash表在存储hash值的时候需要计算存到哪个index。这个计算规则就是hash函数。
>
> 我们知道数组的长度是提前定义好的，假如一个数组的长度是4，不断地给数组添加值，一定会出现多个值放在同一个index上，这就叫hash冲突。如何解决？要么覆盖，要么丢弃。显然这2种都不太合适。

* 4种解决hash冲突的常见方法：

1. **再哈希法**：如果hash出的index已经有值，就再hash，不行继续hash，直至找到空的index位置，要相信瞎猫总能碰上死耗子。这个办法最容易想到。但有2个缺点：
-  比较浪费空间，消耗效率。根本原因还是数组的长度是固定不变的，不断hash找出空的index，可能越界，这时就要创建新数组，而老数组的数据也需要迁移。随着数组越来越大，消耗不可小觑。
  
-  get不到，或者说get算法复杂。进是进去了，想出来就没那么容易了。
2. **开放地址方法：**如果hash出的index已经有值，通过算法在它前面或后面的若干位置寻找空位，这个和再hash算法差别不大。
3. **建立公共溢出区：** 把冲突的hash值放到另外一块溢出区。
4. **链式地址法：** 把产生hash冲突的hash值以链表形式存储在index位置上。HashMap用的就是该方法。优点是不需要另外开辟新空间，也不会丢失数据，寻址也比较简单。但是随着hash链越来越长，寻址也是更加耗时。好的hash算法就是要让链尽量短，最好一个index上只有一个值。也就是尽可能地保证散列地址分布均匀，同时要计算简单。

#### 看源码前的2个准备

###### 1. 复习二进制运算

一次次被虐，主要因为没有深读HashMap源码。而在读源码的过程中经常会看到二进制的相关运算。这里有必要温习一些HashMap中用到的二进制算法。为了不浪费大家的双眸，我们以8位二进制来举例。

* & 与运算，2个二进制数对应的位置都为1结果才为1

  ```java
  如： 10&7
        0000 1010     ---10
    	& 0000 0111			---7
      = 0000 0010     ---2
  ```

* | 或运算：只要有个一个为1，就为1

  ```java
 如：10|7
   		0000 1010     ---10
  	| 0000 0111			---7
    = 0000 1111     ---15
  ```

* ^ 异或：2个二进制数对应的位置不一样，才为1

  ```java
如：1^7
   		0000 1010     ---10
  	^ 0000 0111			---7
    = 0000 1101     ---13
  ```

*   `<<`左移  左移一位都相当于乘以2的1次方，左移n位就相当于乘以2的n次方，前提是数字不能溢出。比如8位操作系统中有一个二进制数是0100 0000，左移一位是1000 0000，是原来数的2倍。但是再左移就成了0000 0000，就不是原数的2倍了。

###### 2. HashMap常见的面试题

 带着问题看源码可能不那么迷茫枯燥。当然还有很多面试题，但只要抓住关键的几点，其他的也就迎刃而解，可谓万不变不离其宗。

* HashMap的底层原理是什么，采用什么数据结构

* HashMap容量为什么必须是2的幂次方

* HashMap什么时候需要进行扩容，扩容如何实现

* HashMap在什么地方会出现线程不安全，如何解决

#### 源码终于来了，本文默认分析JDK1.8的源码，因为网上分析1.7的已经很多了，只在必要时用1.7作为对比。

* 我们宏观了解下HashMap的数据结构。首先它是一个基于hashtable的map。前面我们说到hashtable是存储hash值的数组。可知HahsMap最外层是一个数组。这个数组在源码中的表示是`Node<K,V>[] table`。如下图：![hashmap结构](image/hashmap结构.png)

​      Node是HashMap的内部类，结构如下：

```java
 class Node<K,V> implements Map.Entry<K,V> {
        final int hash;//key的哈希值，用来定位数组索引位置
        final K key;//key
        V value;//value
        Node<K,V> next;//下一个node
        ...........
 }
```

  前面我们又提到，HashMap是通过链式法解决hash冲突。结合Node的结构，HashMap的结构图大概是这样：

![hashmap结构](image/hashnode.png)

HashMap的基本数据结构就是数组+链表。当然在1.8以后又引入了红黑树，我们先不讲，它并不妨碍我们理解HashMap的思想。

除了`table`属性（注：`table[i]=bucket,包含该位置的所有node`），还有几个属性需要提前了解：

```java
		/**
     * The default initial capacity - MUST be a power of two.
     * 数组的初始化容量，必须是2的幂次方，默认16。
     * 为什么不直接写16。鄙人猜测是：可能大师要告诉我们HashMap的容量必须是2的幂次方。
     * 好比我们给某一缓存设置有效时间是3*60s。而不是直接写180秒。其实我们强调的是前面的3分钟。当然这并不重要。
 		*/ 
		static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;

		/**
     * The number of key-value mappings contained in this map.
     * 已经储存的Node<key,value>的数量，包括数组中的和链表中的
     */
    transient int size;

		/**
     * The load factor for the hash table.
     * 负载因子
     */
    final float loadFactor;

		/**
     * The load factor used when none specified in constructor.
     * 负载因子的默认值
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

		/**
     * The javadoc description is true upon serialization.
     * Additionally, if the table array has not been allocated,
     * this field holds the initial array capacity, or zero signifying DEFAULT_INITIAL_CAPACITY.)
     * The next size value at which to resize (capacity * load factor).
     * <p>
     * 扩容的阀值。当size>threshold（=capacity * load factor）的时候就会扩容
     *
     */
    int threshold;

		/**
     * The bin count threshold for untreeifying a (split) bin during a
     * resize operation. Should be less than TREEIFY_THRESHOLD, and at
     * most 6 to mesh with shrinkage detection under removal.
     * 扩容时，如果bucket的node数量小于UNTREEIFY_THRESHOLD，红黑树转为单向链表
     */
    static final int UNTREEIFY_THRESHOLD = 6;

		/**
     * The bin count threshold for using a tree rather than list for a
     * bin.  Bins are converted to trees when adding an element to a
     * bin with at least this many nodes. The value must be greater
     * than 2 and should be at least 8 to mesh with assumptions in
     * tree removal about conversion back to plain bins upon
     * shrinkage.
     * <p>
     * 链表转红黑树的阀值。
     */
    static final int TREEIFY_THRESHOLD = 8;
```

* HashMap中需要剖析的内容很多，但篇幅有限，读者耐心有限，鄙人水平有限。下面仅从hash函数、put方法、扩容过程3个最具代表性的点深入展开讲解。这3点基本涵盖了日常使用和面试的所有场面。

##### 1.hash函数如何计算index。

  32位hash的范围是-2147483648到2147483648，40多亿。只要hash做的好，很难发生碰撞。但是40亿的数组内存是放不下的，只能进行压缩。最简单的方法就是取模，hash%table.length就能计算出index。我们能想到，大师也想到了。只是大师想的更周全。在1.7中有这么一个方法

```java
static int indexFor(int h, int length) {//jdk1.7的源码，jdk1.8没有这个方法，但实现过程一样
     return h & (length-1);
}
```

因为length是2的幂次，才有`hash &（length-1）`等价于`hash % length`，只是在效率上比取模要高，至于为什么，大家可以课下推导，或者找相关文章。这也是map容量为什么必须是2的幂次的一个原因。除了这个原因，此时如果length不是2的幂次会是什么情况？

```java
1. 假如length是一个奇数n，参与hash运算的（n-1）一定是偶数，偶数的最低位一定是0，0和任何数&都为0。所以hash&（n-1）最低位是0，也就是偶数。这样数组的奇数位就浪费了。举个最容易理解的例子，假如length=3。参与hash运算的就是3-1=2。
现在有一个任意hash值 ：1010 1111 0010 0101 1111 1111 1110 0111
  								& 0000 0000 0000 0000 0000 0000 0000 0010 	---2
  								= 0000 0000 0000 0000 0000 0000 0000 0010	  ---2
不管hash值是多少。和2于运算之后，要么是0，要么是2。永远轮不到1。 
2. 假如length是一个非2的幂次的偶数n。n=6
 现在有一个任意hash值 ：1010 1111 0010 0101 1111 1111 1110 0111		
  								 & 0000 0000 0000 0000 0000 0000 0000 0101 	---5
  								 = 0000 0000 0000 0000 0000 0000 0000	0101  ---5
5「101」的中间的「0」和任何数&运算后的结果都是0。意味着index低位第二位永远是0。此时index永远不可能是2或3。有些基础差的同学可能不太理解，如果你知道二进制如何转成十进制就很好理解。如果不知道，我们列举出hash值和5「101」做与的所有可能。因为101前面都是0，所以hash对应位置不管是什么数，&后都为0。我们只关注hash低3位。
h     000   001   011   111   110   100   101
n-1 & 101 & 101 & 101 & 101 & 101 & 101 & 101
    = 000   001   001   101   100   100   101  ---二进制
    =  0     1     1     5     4     4     5   ---十进制
 从结果中可以看出index没有2和3，n越大空位越多。  
  
3. 如果length是2的幂次，那length-1的低位全是1。就不会出现永远为空的位置。比如默认容量16。
现在有一个任意hash值 ：1010 1111 0010 0101 1111 1111 1110 0111		
  							 &  0000 0000 0000 0000 0000 0000 0000 1111 	---15
  							  = 0000 0000 0000 0000 0000 0000 0000 0111   ---7
hash高位全部归零，具体落到哪个角标位，主要取决于hash值的后几位。
```

这是第二个length是2的幂次的原因：数组各个位置不浪费，雨露均沾。

length的作用已经发挥到了极致。刚才提到hash&（length-1），真正起作用的是hash的低位，它是否均匀决定了整个数组分配是否均匀。不能保证key.hashCode()非常合理，如果hash后几位一样，会碰的头破血流。所以HashMap对它做了进一步优化，让hash低位更均匀。

```java
		/**
     * Computes key.hashCode() and spreads (XORs) higher bits of hash
     * to lower.  Because the table uses power-of-two masking, sets of
     * hashes that vary only in bits above the current mask will
     * always collide. (Among known examples are sets of Float keys
     * holding consecutive whole numbers in small tables.)  So we
     * apply a transform that spreads the impact of higher bits
     * downward. There is a tradeoff between speed, utility, and
     * quality of bit-spreading. Because many common sets of hashes
     * are already reasonably distributed (so don't benefit from
     * spreading), and because we use trees to handle large sets of
     * collisions in bins, we just XOR some shifted bits in the
     * cheapest possible way to reduce systematic lossage, as well as
     * to incorporate impact of the highest bits that would otherwise
     * never be used in index calculations because of table bounds.
     * 扰动函数
     */
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

首先将hashCode无符号右移16位，然后再做异或。[这里参考了胖哥的知乎回答](https://www.zhihu.com/question/20733617/answer/111577937)

![index计算](/Users/zhaoyancheng/data/sparrow/javaCore/src/com/cowbi/spqrrow/java/hashmap/md/image/index计算.png)

java中int是32bit，右移16位正好是一半。这样就做到了hash高位于低位的混合，以此来提高低位的随机性。同时也保留了高位的部分特征。胖哥文中提到的实验也证明了混合后的hash要比原始hash冲突概率小。但也不能完全避免hash冲突，我们前文提到，好的hash函数不仅要散列均匀，也要保证高效。

如果效率太低，put，get等相关操作时消耗太大则本末倒置。HashMap设计目标是简洁与高效。

这里还有一个小问题。为什么此时高位和低位要做^，而非`&`或者`｜`？其实不难想到，`&`运算计算出来的值会向0靠拢，而`|`运算计算出来的值会向1靠拢。造成散列不均匀。

**总之，不管用什么方式，最终目的都是散列均匀，效率高。**

##### 2. 详解put方法

```java
		/**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>key</tt>.
     * (A <tt>null</tt> return can also indicate that the map
     * previously associated <tt>null</tt> with <tt>key</tt>.)
     * <p>
     */
    @Override
    public V put(K key, V value) {
        //对key.hashCode做hash
        return putVal(hash(key), key, value, false, true);
    }

    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K, V>[] tab;//暂存table
        Node<K, V> p;//tab[i]
        int n; //tab数组长度
        int i; //数组下标index

        //1. table为空，调用resize()新建
        if ((tab = table) == null || (n = tab.length) == 0) {
            n = (tab = resize()).length;
        }
        //2. 计算index值
        i = (n - 1) & hash;
        //3. 赋值
        if ((p = tab[i]) == null) {
            //如果tab[i]为空，新建一个。
            tab[i] = newNode(hash, key, value, null);
        } else {
            //如果tab[i]上已经有元素，也就是发生了碰撞。
            Node<K, V> e;
            K k;
            //（1）如果hash和key都相等
            if (p.hash == hash &&
                    ((k = p.key) == key || (key != null && key.equals(k)))) {
                e = p;
            } else if (p instanceof TreeNode) {
                //判断该链是否为红黑树，入树
                e = ((TreeNode<K, V>) p).putTreeVal(this, tab, hash, key, value);
            } else {
                //单向链表遍历，index相同，key不同
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) {//如果next为空，追加到链表尾部break
                        p.next = newNode(hash, key, value, null);
                        //链表长度大于8转换为红黑树
                        if (binCount >= TREEIFY_THRESHOLD - 1) {
                            treeifyBin(tab, hash);
                        }
                        break;
                    }
                    //如果key已存在，直接 break
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k)))) {
                        break;
                    }
                    //继续遍历next
                    p = e;
                }
            }
            // 返回oldValue
            if (e != null) {
                V oldValue = e.value;
                //put的入参onlyIfAbsent=false，所以!onlyIfAbsent一直成立
                if (!onlyIfAbsent || oldValue == null) {
                    e.value = value;
                }
                return oldValue;
            }
        }
        ++modCount;
        //4. 超过最大容量限制，扩容
        if (++size > threshold) {
            resize();
        }
        return null;
    }
```

整个put流程还是比较清晰简单的，总体步骤如下：

1. 判断table是否为空，为空就调用resize()初始化数组。HashMap的构造函数并没有初始化数组，而是在put方法初始化数组。
2. 计算index值
3. 判断p=table[i]是否为空，如果是空，直接new Node放到该bucket（table[i]对应的链表）。
4. If不为空
   1. 如果p的hash和key与参数中的hash、key都相等，把p赋值给e。不新建Node。
   2. 如果p是红黑树，把新Node加入树中。
   3. 如果是单向列表，则遍历，把新Node插到bucket末端，如果bucket长度大于8，链表转换成红黑树。
5. 如果size>threshol就扩容。

#### 3. resize扩容机制

一般情况下，当size > threshold的时候就需要扩容，我们知道java的数组是无法真正扩容的。只能新建一个更大的数组来替代之前的老数组，新数组的容量是上一个数组容量的2倍，同样可以保证是2的幂次。

**注意扩容的目的并不是让数组容纳更多的量（理论上链表可以无限长），而是为了提高HashMap的查找、插入效率。**

下面是扩容源码。

```java
		/**
     * Initializes or doubles table size.  If null, allocates in
     * accord with initial capacity target held in field threshold.
     * Otherwise, because we are using power-of-two expansion, the
     * elements from each bin must either stay at same index, or move
     * with a power of two offset in the new table.
     * 初始化或把table的size扩大2倍.
     * 如果是null，就是需要初始化，在put的方法我们见过。初始化做的就是初始capacity和对应的threshold
     * 否则，老table中的元素在向新数组的迁移中，元素要么落在新数组相同的index下。要么落在capacity+index下。
     * @return the table
     */
    final Node<K, V>[] resize() {
        Node<K, V>[] oldTab = table;//创建一个oldTab数组用于保存之前的数组
        int oldCap = (oldTab == null) ? 0 : oldTab.length;//获取原来数组的长度
        int oldThr = threshold;//原来数组扩容的临界值
        int newCap, newThr = 0;
        if (oldCap > 0) {
            //如果原来的数组长度大于最大值(2^30)，不再扩容
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            //新的数组容量为原来的2倍
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                    oldCap >= DEFAULT_INITIAL_CAPACITY) {
                newThr = oldThr << 1; // double threshold
            }
        } else if (oldThr > 0) // initial capacity was placed in threshold
        {
            newCap = oldThr;
        } else {
            //oldThr == 0,初始化cap和threshold
            newCap = DEFAULT_INITIAL_CAPACITY;//新数组初始容量设置为默认值
            newThr = (int) (DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);//计算默认容量下的阈值
        }
        // 计算新的resize上限
        if (newThr == 0) {
            float ft = (float) newCap * loadFactor;//ft为临时变量，用于判断阈值的合法性
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float) MAXIMUM_CAPACITY ?
                    (int) ft : Integer.MAX_VALUE);//计算新的阈值
        }
        threshold = newThr;//改变threshold值为新的阈值

        Node<K, V>[] newTab = (Node<K, V>[]) new Node[newCap];
        table = newTab;
        //扩容，深度复制 bucket
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<K, V> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;//释放老数组元素
                    if (e.next == null) {
                        //将e也就是oldTab[j]放入newTab中e.hash & (newCap - 1)的位置
                        newTab[e.hash & (newCap - 1)] = e;
                    } else if (e instanceof TreeNode) {//红黑树处理
                        ((TreeNode<K, V>) e).split(null, newTab, j, oldCap);
                    } else { // preserve order
                        //lo=low hi=high
                        Node<K, V> loHead = null, loTail = null;
                        Node<K, V> hiHead = null, hiTail = null;
                        Node<K, V> next;
                       //遍历整个单向链表，元素新index，要么是原来index，要么是（index+oldCap）
                        do {
                            next = e.next;
                            /** 
                             e.hash & oldCap == 0 ，node放在原来的index上。why？
                             1. 由于oldCap是2的幂次。它的二进制表示是1个1后面有n个0。比如16的二机制是0001 0000（8位为例）
                             2. 要想 hash & oldCap == 0，那hash二进制中与0001 0000中「1：从低位数第5位」的对应位置必须是																0，其他位置是什么无所谓。所以hash格式是 ***0 ****（从低位数第5位是0）。为了更加直观，我们按照推																演出来的hash规则，随机给hash取个值1110 1010，满足0001 0000 & 1110 1010 = 0。
                             3. 我们知道table的index =（oldCap-1）& hash。
                             （oldCap - 1） & hash
                             = 15 & hash
                             = 0000 1111 & 1110 1010
                             = 0000 1010 = 10 
                             node在原数组的index为10。
                             4. 现在resize，newCap=oldCap*2。
                             index =（2*oldCap-1）& hash
                             =（2*16-1）&hash
                             = 0001 1111 & 1110 1010
                             = 0000 1010 = 10
                             扩容后node在新数组的下标也是10。
                             其实可以明显看到，index就是hash后4位的值。因为这4位的（length-1）都为1，其余位置都为0。
                             5. 所以(e.hash & oldCap) == 0 的时候 node在老数组中的位置与在新数组中的位置一致。
                              此时hash从低位数第5位是0。
                              下面else源码中的hash此位置为1。
                             */
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null) {
                                    loHead = e;
                                } else {
                                    loTail.next = e;
                                }
                                loTail = e;
                            }
                            /**
                             * else 原索引+oldCap
                             * 1. 假设oldCap依然是16,hash = 1111 1010。
                             * 上面已经计算过此hash原始index为10。
                             * 2. 扩容后
                             *  index =（2oldCap-1）& hash。
                             *        = 31 & hash
                             *        = 0001 1111 & 1111 1010
                             *        = 0001 1010 = 26（oldIndex+oldCap）
                             * 我们细品一下 newIndex为什么等于（oldIndex+oldCap），其实也很简单。
                             * oldIndex就是hash后4位的值。这个我们上面已经推演过。
                             * 本次2*length-1的后四位也都是1，所以newIndex值的后4位也是hash后4位。
                    				 * 本次不同的是从低位数第5位，hash此位是1，2*length-1的此位也是1，&运算以后此位置为1.
                             * 所以结果就是1后面跟上hash的后4位，就是11010，也就是10000 +0 1010。
                             *
                             * 总结，newIndex的值取决于hash从低位数第5位的值。
                             * 如果这个位置是0，index不变。如果是1，index=index+oldCap。
                             * 通过简单的&运算就能确定index，无需像jdk1.7那样重新计算index值。这也是为什么HashMap容量是2的															 * 幂次的一个原因。
                             * 有心的同学可能注意到了，index主要取决与hash的最后几位，如果hash算法做的不好，就会加大hash碰															 * 撞。所以this.hash()「扰动函数」做了些优化。
                             * */
                            else {
                                if (hiTail == null) {
                                    hiHead = e;
                                } else {
                                    hiTail.next = e;
                                }
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        // 原索引放到bucket里，尾插法
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        // 原索引+oldCap放到bucket里，尾插法
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }
```

#### 线程安全

HashMap是线程不安全的。多线程情况下会出现很多问题，比较常见的问题就是丢失数据。当有一个线程改动了散列表的结构，都有可能造成另一个线程的操作失败，主要集中在put和resize方法中。更多问题需要大家去探究，只是探究，别以身试法。多线程环境下建议用线程安全的ConcurrentHashMap或者synchronizedMap。

**比如1.8在put方法中**

```java
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        ...省略部分源码..
        //，向同一个位置存入元素，会有值覆盖的问题，导致数丢失。
        if ((p = tab[i]) == null) {
            tab[i] = newNode(hash, key, value, null);
        } else {
       ...省略部分源码..
 }      
```

**1.8在扩容时：**

```java
// 给同一个数组的相同位置赋值，会有数据覆盖的风险
if (loTail != null) {
    loTail.next = null;
    //将原始索引位的数据迁移到新数组1
    newTab[j] = loHead;  
}
if (hiTail != null) {
    hiTail.next = null;
    //将原始索引位的数据迁移到新数组2
    newTab[j + oldCap] = hiHead;
}
```

#### 再见面试题&小结

文章开始提到的几个问题应该已经有了答案。

* HashMap的底层原理是什么，采用什么数据结构

  基于HashTable，最外层是一个Node[]，每个Node可能是单向链表，或者红黑树（列表长度大于8时转红黑树）。

* HashMap容量为什么必须是2的幂次方

  1. 在计算index时，`hash&（length-1）`等价于`hash % length`，但要比直接取模效率高。范围当然也是0至length-1。
  2. 还是`hash&（length-1）`，当length是2的幂次时，数组各个位置不浪费，能做到雨露均沾。其他数都有可能造成空间浪费。
  3. 在扩容时，无需重新hash再计算index，通过和hash&cap就能确定元素在扩容后数组的位置。`&`要比重新hash效率高的多。

* HashMap什么时候需要进行扩容，扩容如何实现

  当size>threshold（capacity * load factor）的时候扩容。简单的说，1.7采用的是头插法扩容，1.8采用的是尾插法进行扩容。具体源码比较简单，大家可以下来自己看，文中也给出了关键代码的注解。多线程时，头插法可能出现的问题是死循环。尾插法的问题是丢失数据。当然其他问题还有待大家发现，如果已经发现，不妨在发表在留言区，大家一起讨论，集思广益。

* HashMap在什么地方会出现线程不安全，如何解决

  由于没做同步，线程不安全随处可见。但最致命的是在put和resize发生数据结构变更的地方。解决方案就是用ConcurrentHashMap或者synchronizedMap替代。

#### 本文参考

本文参考了诸多大神的文章，站在巨人的肩膀上希望能看的更高，进步更大。

由于本人才疏学浅，难免会有若干谬误，欢迎指正。

有问题在留言区谈论，如果觉得「还可以」，期望点赞转发。

1. JDK1.7&JDK1.8 源码。

2. Java Code Geeks，[HashMap performance improvements in Java 8](http://www.javacodegeeks.com/2014/04/hashmap-performance-improvements-in-java-8.html)，2014。

3. [深入理解 hashcode() 和 HashMap 中的hash 算法](https://blog.csdn.net/q5706503/article/details/85114159)

4. [JDK 源码中 HashMap 的 hash 方法原理是什么？](https://www.zhihu.com/question/20733617)

5. [Java 8系列之重新认识HashMap](https://tech.meituan.com/2016/06/24/java-hashmap.html)

   