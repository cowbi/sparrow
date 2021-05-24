# 和前女友复合的那个晚上，我彻底想通了CAS

## 这是一个伤感的微故事

当年忽然消失，拿走我2w元的前女友又忽然回来找我。我心情是复杂的，本以为她要还钱，没想到是复合。我们长谈到深夜，主要是她在聊自己的经历，我听完， 好言相劝她还钱，她甩给我2w，我假装生气，夺门而出。 现已物是人非，2w块钱还是很香，像捡来的一样。

出来抽烟的我似乎想通了CAS的ABA问题。

## 什么是CAS

CAS（Compare And Swap），比较替换。它有3个核心参数：

1. V： 要修改的变量
2. A ：V的原有值，即线程读取V时的值
3. B：要修改成的值

在对V做修改之前，先检查V的值是否与预期值A相等，如果相等就把V值更新为B，否则不更新，CAS失败。

下面是一段伪代码帮助大家理解

```java
//把a的值从3改成4,这里V就是a，A是3，B是4
int a = 3;
cas(a,3,4);
```

先用3与a此刻的值（a所指向的内存值）比较，如果相等，则a会被修改成4。 否则修改失败。失败后如何处理？

它常与自旋（通常用死循环实现）配合使用，修改失败则不停重试CAS，直到成功。整个过程无需锁定a，是一种乐观锁的实现。检测

冲突和数据更新本身也是乐观锁的一贯作风。

## 解决线程同步

解决线程同步的方式一般有2种

1. 悲观锁
2. 乐观锁

悲观锁的缺陷很明显，比如死锁、加锁释放锁带来的性能消耗。

用乐观锁在大部分情况下可以提高性能，上面说的CAS就是一种乐观锁的实现。但CAS只能解决多线程的原子性，并不能解决另外2个问

题，顺序性和一致性。而volatile正好能解决这2个问题，你说巧不巧。所以CAS和volatile必定能走到一起。

当然java大师们早已发现了其中的奥秘。用CAS+volatile作为基石开发出了大名鼎鼎的J.U.C包。

其中最简单直观的要数atomic包。

多线程场景下，给某个数做自增的时候通常是加synchronized关键字

```java
int count = 0;
synchronized void c() {
		for (int i = 0; i < 10000; i++)
			count++;
}
```

JUC包中的`AtomicInteger`提供的`incrementAndGet` 完全可以代替synchronized和count++的功能。

```java
AtomicInteger count = new AtomicInteger(0);
void c() {
	for (int i = 0; i < 10000; i++)
			count.incrementAndGet();
}
```

AtomicInteger：

```java
//value被volatile修饰
private volatile int value;
public AtomicInteger(int initialValue) {
        value = initialValue;
}


public final int incrementAndGet() {
    return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
}

public final int getAndAddInt(Object var1, long var2, int var4) {
        int var5;
        do {
            //获取预期值
            var5 = this.getIntVolatile(var1, var2);
        } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));

        return var5;
 }

```

Unsafe：

```java
public final native boolean compareAndSwapInt(Object var1, long var2, int var4, int var5);
```

count.incrementAndGet最终调用的方法是Unsafe类的compareAndSwapInt方法，这个方法被native修饰。也就意味着它已经调用了

虚拟机C/C++实现的代码。笔者试着跟了一下Hotspot的代码，具体过程就不演示了。最后代码如下：

```c++
inline jint Atomic::cmpxchg (jint exchange_value, volatile jint*dest, jint compare_value) {
  int mp = os::is_MP();
  __asm__ volatile (LOCK_IF_MP(%4) "cmpxchgl %1,(%3)"
                    : "=a" (exchange_value)
                    : "r" (exchange_value), "a" (compare_value), "r" (dest), "r" (mp)
                    : "cc", "memory");
  return exchange_value;
}
```

关键看LOCK_IF_MP：如果是多处理器要加lock，保证CAS的原子性，最终执行的是汇编指令：cmpxchg（compare and exchange）。

```assembly
lock cmpxchg 
```

汇编是可以直接操作CPU的，cmpxchg指令也是cpu支持的原语。但cmpxchg并不是原子的，所以在多处理器下还需要加lock老保证CAS的原子性。

有同学说了，这还不是加锁了。是的，只是在系统级别加的锁，要比在jvm上加锁效率高的多。

我们说的CAS无锁并不是绝对的无锁，而是在java层面表现为无锁。

**看不懂c++、汇编指令不要紧，但是`lock cmpxchg ` 一定要记住，因为面试要问。**

## CAS的ABA问题

**ABA ： 数据从A变成B，又变回A，假装什么都没发生。**

上面的例子中，如果a从3变为5，又变回3，那么CAS在对比时a的值依然是3，替换成功。

这好像也没什么问题，反正目的就是把a的值改成4。就像前女友还我的2万块钱，虽然RMB已经改版，新版2w依然可以接受。

但不是所有人都能接受失而复得。

一般基础类型的ABA问题大家都能接受。但是复杂类型就不一定了，比如下面的Zoo类

```java
public class Zoo {
    private Tiger tiger = new Tiger("母老虎");
    public static void main(String[] args) {
        Zoo zoo = new Zoo();
        zoo.setTiger(new Tiger("公老虎"));
    }
}
```

Zoo里面有成员变量tiger。当new一个Zoo实例zoo的时候，zoo的内存值已经确定了，不会因为其成员变量的改变而改变。

这样的ABA问题就比较严重。就像我的前任一样。

解决ABA问题通用的办法就是加版本号，A->B->A改成A1-B2->A3。JUC用带版本号的 `AtomicStampedReference` 来解决ABA问题。

```java
//initialRef:要修改的引用V，initialStamp：版本号，可以是时间戳或任何整数
public AtomicStampedReference(V initialRef, int initialStamp) {
    pair = Pair.of(initialRef, initialStamp);
}
```

本文着重是理论分析，而且AtomicStampedReference比较简单，就不做展开讨论。

ABA终于想通了 ，多么痛的领悟，你的前任回来找你了吗。记得注意是否存在ABA问题哦。

（本故事纯属虚构，如有雷同，请不要对号入座）。