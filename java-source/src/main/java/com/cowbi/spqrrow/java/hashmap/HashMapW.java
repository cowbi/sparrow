package com.cowbi.spqrrow.java.hashmap;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 提取jdk1.8.0_201的精华部分进行详解。包括了HashMap关（面）注（试）点。本类并没有实现红黑树
 * 1. hashmap的容量为什么是2的幂次方数? 答案详情看方法注解
 * （1）计算index=hash&(length-1) 相当于hash%length，不仅提高了效率，也保证了散列的均匀。
 * 既然length是2的n次幂，那它一定是偶数，length-1一定是奇数。二进制低位一定是1。这样hash&(length-1)的尾数可能是0或者1。即是偶数也可能是奇数。
 * 但是如果length是奇数，那length-1一定是偶数。偶数最后一位一定是0，那hash&(length-1)一定是0。即是偶数。这样任何数都会被分配到偶数的下标位，浪费了奇数下标位。
 * 因此，length取2的整数次幂，是为了使不同hash值发生碰撞的概率较小，这样就能使元素在哈希表中更均匀地散列。
 * <p>
 * 而当数组长度为16时，即为2的n次方时，n-1得到的二进制数的每个位上的值都为1，这使得在低位上&时，得到的和原hash的低位相同（提供既高效又纯粹的取模），
 * 加之hash(int h)方法对key的hashCode的进一步优化，加入了高位计算，就使得只有相同的hash值的两个值才会被放到数组中的同一个位置上形成链表。
 * <p>
 * （2）resize 从新计算index时
 * （3）hash(key)
 * (4)tableSizeFor()
 * 2. 线程不安全体现在哪? put，resize()，删除操作、修改操作，同样都会有覆盖问题。
 * （1）当线程A和线程B都获取到了bucket的头结点后，若此时线程A的时间片用完，线程B将其新数据完成了头插法操作，此时轮到线程A操作，
 * 但这时线程A所据有的旧头结点已经过时了（并未包含线程B刚插入的新结点），线程A再做头插法操作，就会抹掉B刚刚新增的结点，导致数据丢失。
 * （2）resize在1.7及之前版本会导致死循环。1.8顶多一次循环。
 * 1.7头插法: 线程a和线程b同时扩容，假设它们在相同的bucket上,在a线程完成扩容后（b已经完成put）。所以此时该bucket上的链表是a->b
 * 当b扩容的时候，就成了b—>a—>b。这时就出现了死循环。a的next指向b，b的next指向a。
 * 在get的是就会报错：Infinite Loop
 * 3. hashmap数据结构? 数组+链表+红黑树
 * <p>
 * 好的哈希函数会尽可能地保证计算简单和散列地址分布均匀。
 * 但是，数组是一块连续的固定长度的内存空间，再好的哈希函数也不能保证得到的存储地址绝对不发生冲突。
 * 如何解决hash冲突：哈希冲突的解决方案有多种:开放定址法（发生冲突，继续寻找下一块未被占用的存储地址），再散列函数法，链地址法，而HashMap即是采用了链地址法，也就是数组+链表的方式
 * <p>
 * <p>
 * 哈希表是非线程安全的， 如果多线程同时访问哈希表， 且至少一个线程修改了哈希表的结构，
 * 那么必须在访问hashmap前设置同步锁。（修改结构是指添加或者删除一个或多个entry， 修改键值不算是修改结构。）
 * 一般在多线程操作哈希表时，  要使用同步对象封装map。
 * 如果不封装Hashmap， 可以使用Collections.synchronizedMap方法调用HashMap实例。
 * 在创建HashMap实例时避免其他线程操作该实例，即保证了线程安全。
 * 当然也可以直接用使用ConcurrentHashMap。在jdk1.5之前使用的hashtable。
 *
 * @author zyc
 * @data 2020-11-26
 * @see java.util.HashMap
 */
public class HashMapW<K, V> extends AbstractMap<K, V>
        implements Map<K, V>, Cloneable, Serializable {

    private static final long serialVersionUID = 2586712385947688968L;

    /*
     * Implementation notes.
     *
     * This map usually acts as a binned (bucketed) hash table, but
     * when bins get too large, they are transformed into bins of
     * TreeNodes, each structured similarly to those in
     * java.util.TreeMap. Most methods try to use normal bins, but
     * relay to TreeNode methods when applicable (simply by checking
     * instanceof a node).  Bins of TreeNodes may be traversed and
     * used like any others, but additionally support faster lookup
     * when overpopulated. However, since the vast majority of bins in
     * normal use are not overpopulated, checking for existence of
     * tree bins may be delayed in the course of table methods.
     *
     * Tree bins (i.e., bins whose elements are all TreeNodes) are
     * ordered primarily by hashCode, but in the case of ties, if two
     * elements are of the same "class C implements Comparable<C>",
     * type then their compareTo method is used for ordering. (We
     * conservatively check generic types via reflection to validate
     * this -- see method comparableClassFor).  The added complexity
     * of tree bins is worthwhile in providing worst-case O(log n)
     * operations when keys either have distinct hashes or are
     * orderable, Thus, performance degrades gracefully under
     * accidental or malicious usages in which hashCode() methods
     * return values that are poorly distributed, as well as those in
     * which many keys share a hashCode, so long as they are also
     * Comparable. (If neither of these apply, we may waste about a
     * factor of two in time and space compared to taking no
     * precautions. But the only known cases stem from poor user
     * programming practices that are already so slow that this makes
     * little difference.)
     *
     * Because TreeNodes are about twice the size of regular nodes, we
     * use them only when bins contain enough nodes to warrant use
     * (see TREEIFY_THRESHOLD). And when they become too small (due to
     * removal or resizing) they are converted back to plain bins.  In
     * usages with well-distributed user hashCodes, tree bins are
     * rarely used.  Ideally, under random hashCodes, the frequency of
     * nodes in bins follows a Poisson distribution
     * (http://en.wikipedia.org/wiki/Poisson_distribution) with a
     * parameter of about 0.5 on average for the default resizing
     * threshold of 0.75, although with a large variance because of
     * resizing granularity. Ignoring variance, the expected
     * occurrences of list size k are (exp(-0.5) * pow(0.5, k) /
     * factorial(k)). The first values are:
     *
     * 0:    0.60653066
     * 1:    0.30326533
     * 2:    0.07581633
     * 3:    0.01263606
     * 4:    0.00157952
     * 5:    0.00015795
     * 6:    0.00001316
     * 7:    0.00000094
     * 8:    0.00000006
     * more: less than 1 in ten million
     *
     * 到8个的时候，概率已经很低。没必要再多，泊松分布。
     *
     * The root of a tree bin is normally its first node.  However,
     * sometimes (currently only upon Iterator.remove), the root might
     * be elsewhere, but can be recovered following parent links
     * (method TreeNode.root()).
     *
     * All applicable internal methods accept a hash code as an
     * argument (as normally supplied from a public method), allowing
     * them to call each other without recomputing user hashCodes.
     * Most internal methods also accept a "tab" argument, that is
     * normally the current table, but may be a new or old one when
     * resizing or converting.
     *
     * When bin lists are treeified, split, or untreeified, we keep
     * them in the same relative access/traversal order (i.e., field
     * Node.next) to better preserve locality, and to slightly
     * simplify handling of splits and traversals that invoke
     * iterator.remove. When using comparators on insertion, to keep a
     * total ordering (or as close as is required here) across
     * rebalancings, we compare classes and identityHashCodes as
     * tie-breakers.
     *
     * The use and transitions among plain vs tree modes is
     * complicated by the existence of subclass LinkedHashMap. See
     * below for hook methods defined to be invoked upon insertion,
     * removal and access that allow LinkedHashMap internals to
     * otherwise remain independent of these mechanics. (This also
     * requires that a map instance be passed to some utility methods
     * that may create new nodes.)
     *
     * The concurrent-programming-like SSA-based coding style helps
     * avoid aliasing errors amid all of the twisty pointer operations.
     */

    /**
     * The default initial capacity - MUST be a power of two.
     * 数组的初始化容量，必须是2的n次幂，默认16。
     * 为什么不直接写16。鄙人猜测是，可能大师要告诉我们HashMap的容量必须是2的幂次方。
     * 好比我们给某一缓存设置有效时间是3*60s。而不是直接写180秒。其实我们强调的是前面的3分钟。
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;


    /**
     * The load factor used when none specified in constructor.
     * 负载因子的默认值
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     * <p>
     * 最大容量为2的30次方
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor for the hash table.
     * 负载因子
     *
     * @serial
     */
    final float loadFactor;


    /**
     * The number of key-value mappings contained in this map.
     * 已经储存的Node<key,value>的数量，包括数组中的和链表中的
     */
    transient int size;

    /**
     * The number of times this HashMap has been structurally modified
     * Structural modifications are those that change the number of mappings in
     * the HashMap or otherwise modify its internal structure (e.g.,
     * rehash).  This field is used to make iterators on Collection-views of
     * the HashMap fail-fast.  (See ConcurrentModificationException).
     * <p>
     * 用来记录HashMap内部结构发生变化的次数，主要用于迭代的快速失败。
     * 强调一点，内部结构发生变化指的是结构发生变化，例如put新键值对，但是某个key对应的value值被覆盖不属于结构变化。
     */
    transient int modCount;

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

    /**
     * The javadoc description is true upon serialization.
     * Additionally, if the table array has not been allocated,
     * this field holds the initial array capacity, or zero signifying DEFAULT_INITIAL_CAPACITY.)
     * The next size value at which to resize (capacity * load factor).
     * <p>
     * <p>
     * 扩容的阀值。当size>threshold（=capacity * load factor）的时候就会扩容
     *
     * @serial
     */
    int threshold;


    /**
     * The table, initialized on first use, and resized as
     * necessary. When allocated, length is always a power of two.
     * (We also tolerate length zero in some operations to allow
     * bootstrapping mechanics that are currently not needed.)
     * <p>
     * 以Node<K,V>为元素的数组，也就是HashMap的纵向的长链数组
     * 在首次用的时候初始化，类似于“懒加载”，用的时候再初始化，这样有利于节省资源
     */
    transient Node<K, V>[] table;

    // For conversion from TreeNodes to plain nodes
    Node<K, V> replacementNode(Node<K, V> p, Node<K, V> next) {
        return new Node<>(p.hash, p.key, p.value, next);
    }


    /**
     * Constructs an empty <tt>HashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    public HashMapW() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
    }

    /**
     * Basic hash bin node, used for most entries.  (See below for
     * TreeNode subclass, and in LinkedHashMap for its Entry subclass.)
     * 绝大部分节点的对象，包括数组对象和链表对象
     */
    static class Node<K, V> implements Map.Entry<K, V> {
        //每个储存元素key的哈希值，用来定位数组索引位置
        final int hash;
        final K key;
        V value;
        //链表下一个node
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override
        public final K getKey() {
            return key;
        }

        @Override
        public final V getValue() {
            return value;
        }

        @Override
        public final String toString() {
            return key + "=" + value;
        }

        @Override
        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        @Override
        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        @Override
        public final boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof Map.Entry) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                if (Objects.equals(key, e.getKey()) &&
                        Objects.equals(value, e.getValue())) {
                    return true;
                }
            }
            return false;
        }
    }


    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }

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
        return putVal(hash(key), key, value, false, true);
    }

    /**
     * Implements Map.put and related methods.
     *
     * @param hash         hash for key
     * @param key          the key
     * @param value        the value to put
     * @param onlyIfAbsent if true, don't change existing value
     * @param evict        if false, the table is in creation mode.
     * @return previous value, or null if none 以前的值，如果以前没值，返回null
     */
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K, V>[] tab;//暂存table
        Node<K, V> p;//tab[i]
        int n; //tab数组长度
        int i; //数组下标index

        /**
         * 1. tab为空，就调用resize()新建
         *  resize()最终执行的就是 new Node[DEFAULT_INITIAL_CAPACITY]
         * */
        if ((tab = table) == null || (n = tab.length) == 0) {
            n = (tab = resize()).length;
        }
        /**
         * 2. 计算i值，也就是数组下标
         * (n - 1)&hash 在n为2的次幂的时候等价于 hash%n，但是要比hash%n效率高。
         * 这也是为什么capacity必须为2的次幂的一个原因
         * */
        i = (n - 1) & hash;
        //3. 赋值
        if ((p = tab[i]) == null) {
            //如果tab[i]为空，新建一个。
            tab[i] = newNode(hash, key, value, null);
        } else {
            //如果tab[i]上已经有元素了，也就是发生了碰撞。这个时候就要具体情况分
            Node<K, V> e;
            K k;
            //（1）如果hash和key都相等，则覆盖原来位置的node
            if (p.hash == hash &&
                    ((k = p.key) == key || (key != null && key.equals(k)))) {
                e = p;
            } else if (p instanceof TreeNode) {//判断该链是否为红黑树
                e = ((TreeNode<K, V>) p).putTreeVal(this, tab, hash, key, value);
            } else {
                //链表遍历，是链表，index相同，key不同
                //注意：此时p是tab[i]上的node
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) {//如果next为空，追加到链表尾部break
                        p.next = newNode(hash, key, value, null);
                        //链表长度大于8转换为红黑树
                        if (binCount >= TREEIFY_THRESHOLD - 1) {
                            treeifyBin(tab, hash);
                        }
                        break;
                    }
                    //如果key已存在，直接break。在line274处更新值
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k)))) {
                        break;
                    }
                    //继续遍历next
                    p = e;
                }
            }
            // 返回oldValue，key对应的上一个value
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
     * <p>
     * 扰动函数
     * 右位移16位，正好是32bit的一半，自己的高半区和低半区做异或，就是为了混合原始哈希码的高位和低位，以此来加大低位的随机性。
     * 而且混合后的低位掺杂了高位的部分特征，这样高位的信息也被变相保留下来。
     */
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }


    /**
     * Initializes or doubles table size.  If null, allocates in
     * accord with initial capacity target held in field threshold.
     * Otherwise, because we are using power-of-two expansion, the
     * elements from each bin must either stay at same index, or move
     * with a power of two offset in the new table.
     * 初始化数组或者扩容为2倍。
     *
     * <p>
     * 扩容的目的不是为了要多放元素，因为列表可以无限扩大。是为了解决列表过长，查找变慢的问题。
     * <p>
     * 线程不安全就发生在这里
     *
     * @return the table
     */
    final Node<K, V>[] resize() {
        Node<K, V>[] oldTab = table;//创建一个oldTab数组用于保存之前的数组
        int oldCap = (oldTab == null) ? 0 : oldTab.length;//获取原来数组的长度
        int oldThr = threshold;//原来数组扩容的临界值
        int newCap, newThr = 0;
        if (oldCap > 0) {
            //如果原来的数组长度大于最大值(2^30)，把threshold设置成Integer.MAX_VALUE，不再扩容
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            //左移1位相当于乘2，但是比乘2效率更高。oldCap*2同时oldThr*2
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                    oldCap >= DEFAULT_INITIAL_CAPACITY) {
                newThr = oldThr << 1; // double threshold
            }
        } else if (oldThr > 0) // initial capacity was placed in threshold
        {
            newCap = oldThr;
        } else {
            //oldThr == 0,零初始阈值表示使用默认值
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
                    } else if (e instanceof TreeNode) {//红黑树处理，本类并未实现
                        ((TreeNode<K, V>) e).split(null, newTab, j, oldCap);
                    } else { // preserve order 链表优化重hash的代码块
                        //英语lo=low hi=high
                        Node<K, V> loHead = null, loTail = null;
                        Node<K, V> hiHead = null, hiTail = null;
                        Node<K, V> next;
                        do {
                            next = e.next;
                            /**
                             1. 由于oldCap是2的n次幂。它的二进制表示是1个1后面有n个0。比如16的二机制是0001 0000（8位为例）
                             2. 要想 hash & oldCap == 0，那hash二进制中与0001 0000中「1」的对应位置必须是0，其他位置是什么无所谓。
                             所以hash格式是 ***0 ****。为了更加直观，我们按照推演出来的hash规则，随机给hash取个值1110 1010，当然 0001 0000 & 1110 1010 = 0。
                             3. 而我们知道table下标index =（oldCap-1）& hash。
                             （oldCap - 1） & hash
                             = 15 & hash
                             = 0000 1111 & 1110 1010
                             = 0000 1010 = 10
                             4. 现在resize，newCap=oldCap*2。
                             index =（2*oldCap-1）& hash
                             =（2*16-1）&hash
                             = 0001 1111 & 1110 1010
                             = 0000 1010 = 10
                             2次计算后的index都是10。
                             所以(e.hash & oldCap) == 0 的时候 node在老数组中的位置与在新数组中的位置一致。
                             这里可能有朋友有疑问，这个hash个例并不能代表全部。我们继续推演一下
                             oldCap的二进制表示是1后面n个零。（OldCap-1）的二进制是n个1。对齐一下就是0后面加n个1。
                             据上面推论，hash就是0后面有n个任意数，0前面高位可以有任意数，我们可以不考虑，因为（OldCap-1）0前面都是0。&以后都是0。
                             其实index结果是hash的0后面的数决定的。

                             扩容oldCap*2就是oldCap左移一位。对照上面就是1后面跟（n+1）个零。（oldCap*2-1）就是0后面跟（n+1）个1
                             其实（2OldCap-1）与（OldCap-1）相比只是右面多了一个1。而这个1和hash&运算时，正好对应hash的「0」位。所以这个位置&结果还是0。
                             index结果还是由hash的「0」后面的数决定的。

                             所以满足条件的node就依然放在新数组的tab[j]位置
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
                             * 原索引+oldCap
                             * 这时hash刚才的「0」位就得是1。姑且叫它hash「1」位。
                             * 1. 假设oldCap依然是16。hash = 1111 1010
                             * index =（oldCap-1）& hash。
                             * （oldCap - 1） & hash
                             *  = 15 & hash
                             *  = 0000 1111 & 1111 1010
                             *  = 0000 1010 = 10
                             * 2. 扩容后
                             *  index =（2oldCap-1）& hash。
                             *        = 31 & hash
                             *        = 0001 1111 & 1111 1010
                             *        = 0001 1010 = 26（oldIndex+oldCap）
                             * 我们细品一下 newIndex为什么等于（oldIndex+oldCap），其实也很简单。
                             * oldIndex就是hash「1」后面的值。这个我们上面已经推演过。
                             * hash「1」中的1和（2*oldCap）中的1 &后就是1。就相当于1后面n个零+0后面若干数，
                             * 最终结果就是1后面跟若干数
                             *
                             * 所以node在新数组中的下标是 newTab[j + oldCap]
                             *
                             * 总结，所以新的index只判断hash的1bit就可以。无需像jdk1.7那样重新计算index值 rehash。
                             * 有心的同学可能注意到了，index主要取决与hash的最后几位，如果hash算法做的不好，就会加大hash碰撞。
                             * 当然了this.hash()「扰动函数」做了些优化。
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
                        // 原索引放到bucket里
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        // 原索引+oldCap放到bucket里
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

    /* ------------------------------------------------------------ */
    // Tree bins

    /**
     * Entry for Tree bins. Extends LinkedHashMap.Entry (which in turn
     * extends Node) so can be used as extension of either regular or
     * linked node.
     * 红黑树简化版
     */
    static final class TreeNode<K, V> extends Node<K, V> {
        TreeNode<K, V> parent;  // red-black tree links
        TreeNode<K, V> left;
        TreeNode<K, V> right;
        TreeNode<K, V> prev;    // needed to unlink next upon deletion
        boolean red;

        TreeNode(int hash, K key, V value, Node<K, V> next) {
            super(hash, key, value, next);
        }

        /**
         * Tree version of putVal.
         * 没有具体实现，求放过
         */
        final TreeNode<K, V> putTreeVal(HashMapW<K, V> map, Node<K, V>[] tab,
                                        int h, K k, V v) {
            return new TreeNode(h, k, v, null);
        }

        /**
         * Ensures that the given root is the first node of its bin.
         */
        static <K, V> void moveRootToFront(Node<K, V>[] tab, TreeNode<K, V> root) {
            int n;
            if (root != null && tab != null && (n = tab.length) > 0) {
                int index = (n - 1) & root.hash;
                TreeNode<K, V> first = (TreeNode<K, V>) tab[index];
                if (root != first) {
                    Node<K, V> rn;
                    tab[index] = root;
                    TreeNode<K, V> rp = root.prev;
                    if ((rn = root.next) != null)
                        ((TreeNode<K, V>) rn).prev = rp;
                    if (rp != null)
                        rp.next = rn;
                    if (first != null)
                        first.prev = root;
                    root.next = first;
                    root.prev = null;
                }
                assert checkInvariants(root);
            }
        }

        /**
         * Splits nodes in a tree bin into lower and upper tree bins,
         * or untreeifies if now too small. Called only from resize;
         * see above discussion about split bits and indices.
         *
         * @param map   the map
         * @param tab   the table for recording bin heads
         * @param index the index of the table being split
         * @param bit   the bit of hash to split on
         */
        final void split(HashMapW<K, V> map, Node<K, V>[] tab, int index, int bit) {
            TreeNode<K, V> b = this;
            // Relink into lo and hi lists, preserving order
            TreeNode<K, V> loHead = null, loTail = null;
            TreeNode<K, V> hiHead = null, hiTail = null;
            int lc = 0, hc = 0;
            for (TreeNode<K, V> e = b, next; e != null; e = next) {
                next = (TreeNode<K, V>) e.next;
                e.next = null;
                if ((e.hash & bit) == 0) {
                    if ((e.prev = loTail) == null)
                        loHead = e;
                    else
                        loTail.next = e;
                    loTail = e;
                    ++lc;
                } else {
                    if ((e.prev = hiTail) == null)
                        hiHead = e;
                    else
                        hiTail.next = e;
                    hiTail = e;
                    ++hc;
                }
            }

            //低位元素小于6，树形结构转成单向链表
            if (loHead != null) {
                if (lc <= UNTREEIFY_THRESHOLD)
                    tab[index] = loHead.untreeify(map);
                else {
                    tab[index] = loHead;
                    if (hiHead != null) // (else is already treeified)
                        loHead.treeify(tab);
                }
            }
            if (hiHead != null) {
                if (hc <= UNTREEIFY_THRESHOLD)
                    tab[index + bit] = hiHead.untreeify(map);
                else {
                    tab[index + bit] = hiHead;
                    if (loHead != null)
                        hiHead.treeify(tab);
                }
            }
        }

        /**
         * Forms tree of the nodes linked from this node.
         */
        final void treeify(Node<K, V>[] tab) {
            TreeNode<K, V> root = null;
            for (TreeNode<K, V> x = this, next; x != null; x = next) {
                next = (TreeNode<K, V>) x.next;
                x.left = x.right = null;
                if (root == null) {
                    x.parent = null;
                    x.red = false;
                    root = x;
                } else {
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    for (TreeNode<K, V> p = root; ; ) {
                        int dir, ph;
                        K pk = p.key;
                        if ((ph = p.hash) > h)
                            dir = -1;
                        else if (ph < h)
                            dir = 1;
                        else if ((kc == null &&
                                (kc = comparableClassFor(k)) == null) ||
                                (dir = compareComparables(kc, k, pk)) == 0)
                            dir = tieBreakOrder(k, pk);

                        TreeNode<K, V> xp = p;
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;
                            if (dir <= 0)
                                xp.left = x;
                            else
                                xp.right = x;
                            root = balanceInsertion(root, x);
                            break;
                        }
                    }
                }
            }
            moveRootToFront(tab, root);
        }

        /**
         * Recursive invariant check
         */
        static <K, V> boolean checkInvariants(TreeNode<K, V> t) {
            TreeNode<K, V> tp = t.parent, tl = t.left, tr = t.right,
                    tb = t.prev, tn = (TreeNode<K, V>) t.next;
            if (tb != null && tb.next != t)
                return false;
            if (tn != null && tn.prev != t)
                return false;
            if (tp != null && t != tp.left && t != tp.right)
                return false;
            if (tl != null && (tl.parent != t || tl.hash > t.hash))
                return false;
            if (tr != null && (tr.parent != t || tr.hash < t.hash))
                return false;
            if (t.red && tl != null && tl.red && tr != null && tr.red)
                return false;
            if (tl != null && !checkInvariants(tl))
                return false;
            if (tr != null && !checkInvariants(tr))
                return false;
            return true;
        }

        /**
         * Tie-breaking utility for ordering insertions when equal
         * hashCodes and non-comparable. We don't require a total
         * order, just a consistent insertion rule to maintain
         * equivalence across rebalancings. Tie-breaking further than
         * necessary simplifies testing a bit.
         */
        static int tieBreakOrder(Object a, Object b) {
            int d;
            if (a == null || b == null ||
                    (d = a.getClass().getName().
                            compareTo(b.getClass().getName())) == 0)
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
                        -1 : 1);
            return d;
        }

        /**
         * Returns a list of non-TreeNodes replacing those linked from
         * this node.
         * <p>
         * 把原来的树形结构变成单向列表
         */
        final Node<K, V> untreeify(HashMapW<K, V> map) {
            Node<K, V> hd = null, tl = null;
            for (Node<K, V> q = this; q != null; q = q.next) {
                Node<K, V> p = map.replacementNode(q, null);
                if (tl == null)
                    hd = p;
                else
                    tl.next = p;
                tl = p;
            }
            return hd;
        }

        static <K, V> TreeNode<K, V> balanceInsertion(TreeNode<K, V> root,
                                                      TreeNode<K, V> x) {
            x.red = true;
            for (TreeNode<K, V> xp, xpp, xppl, xppr; ; ) {
                if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                } else if (!xp.red || (xpp = xp.parent) == null)
                    return root;
                if (xp == (xppl = xpp.left)) {
                    if ((xppr = xpp.right) != null && xppr.red) {
                        xppr.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    } else {
                        if (x == xp.right) {
                            root = rotateLeft(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateRight(root, xpp);
                            }
                        }
                    }
                } else {
                    if (xppl != null && xppl.red) {
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    } else {
                        if (x == xp.left) {
                            root = rotateRight(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateLeft(root, xpp);
                            }
                        }
                    }
                }
            }
        }

        /* ------------------------------------------------------------ */
        // Red-black tree methods, all adapted from CLR

        static <K, V> TreeNode<K, V> rotateLeft(TreeNode<K, V> root,
                                                TreeNode<K, V> p) {
            TreeNode<K, V> r, pp, rl;
            if (p != null && (r = p.right) != null) {
                if ((rl = p.right = r.left) != null)
                    rl.parent = p;
                if ((pp = r.parent = p.parent) == null)
                    (root = r).red = false;
                else if (pp.left == p)
                    pp.left = r;
                else
                    pp.right = r;
                r.left = p;
                p.parent = r;
            }
            return root;
        }

        static <K, V> TreeNode<K, V> rotateRight(TreeNode<K, V> root,
                                                 TreeNode<K, V> p) {
            TreeNode<K, V> l, pp, lr;
            if (p != null && (l = p.left) != null) {
                if ((lr = p.left = l.right) != null)
                    lr.parent = p;
                if ((pp = l.parent = p.parent) == null)
                    (root = l).red = false;
                else if (pp.right == p)
                    pp.right = l;
                else
                    pp.left = l;
                l.right = p;
                p.parent = l;
            }
            return root;
        }

    }

    /**
     * Returns x's Class if it is of the form "class C implements
     * Comparable<C>", else null.
     */
    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class<?> c;
            Type[] ts, as;
            Type t;
            ParameterizedType p;
            if ((c = x.getClass()) == String.class) // bypass checks
                return c;
            if ((ts = c.getGenericInterfaces()) != null) {
                for (int i = 0; i < ts.length; ++i) {
                    if (((t = ts[i]) instanceof ParameterizedType) &&
                            ((p = (ParameterizedType) t).getRawType() ==
                                    Comparable.class) &&
                            (as = p.getActualTypeArguments()) != null &&
                            as.length == 1 && as[0] == c) // type arg is c
                        return c;
                }
            }
        }
        return null;
    }


    /**
     * Returns k.compareTo(x) if x matches kc (k's screened comparable
     * class), else 0.
     */
    @SuppressWarnings({"rawtypes", "unchecked"}) // for cast to Comparable
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 :
                ((Comparable) k).compareTo(x));
    }


    /*
     * The following package-protected methods are designed to be
     * overridden by LinkedHashMap, but not by any other subclass.
     * Nearly all other internal methods are also package-protected
     * but are declared final, so can be used by LinkedHashMap, view
     * classes, and HashSet.
     */

    // Create a regular (non-tree) node
    Node<K, V> newNode(int hash, K key, V value, Node<K, V> next) {
        return new Node<>(hash, key, value, next);
    }

    /**
     * Returns a power of two size for the given target capacity.
     * <p>
     * 最近的2的幂次方，比如传入10 返回16
     * 如何判断一个数是2的次方数，右移，二进制
     * 为什么要右移这么多次。因为int最高位是32 这些加起来正好32位 1+2+4+8+16
     * 左移一位就是*2
     *
     * @param cap 传递的Capacity值(或者默认的16)
     */
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    // Callbacks to allow LinkedHashMap post-actions
    void afterNodeAccess(Node<K, V> p) {
    }

    void afterNodeInsertion(boolean evict) {
    }


    /**
     * Replaces all linked nodes in bin at index for given hash unless
     * table is too small, in which case resizes instead.
     */
    final void treeifyBin(Node<K, V>[] tab, int hash) {

    }

}
