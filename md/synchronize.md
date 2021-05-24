浅谈synchronized

## 用法

synchronized是元老级的锁，其自动上锁，自动释放锁，简单易用且高效深受广大开发者的喜欢。下面是synchronized的3种用法。

* 锁普通方法，其实就是锁当前对象 *this*

  ```java
  synchronized void sm(){}
  ```

* 锁静态方法，锁的是类的Class对象。

  ```java
  static synchronized void ssm(){}
  ```

* 锁代码块，锁的是传入的对象

  ```java
  synchronized (o){}
  注意 o不能是 string常量，因为常量公用、Integer Long 等基础类型，因为只要变值就会产生一个新对象。
  ```

## 字节码

其实synchronized最终锁的都是对象，任何对象都可以上锁。`synchronized`是关键字，我们不能通过`java`代码来看它的实现原理。

可以先看它编译称class文件的样子。让翠花上代码：

```java
public class SynchronizedClass {

    static synchronized void ssm() {
    }

    synchronized void sm() {
    }

    void m() {
        Object o = new Object();
        synchronized (o) {
        }
    }
}
```

通过 `javap -v SynchronizedClass.class `  查看SynchronizedClass的class字节码，关键代码如下

```java
public com.mashibing.juc.c_025.SynchronizedClass();
  
	//省略部分代码....
	//java文件中的ssm方法
  static synchronized void ssm();
    descriptor: ()V
    flags: ACC_STATIC, ACC_SYNCHRONIZED
    Code:
      stack=0, locals=0, args_size=0
         0: return
      LineNumberTable:
        line 11: 0
	//sm方法
  synchronized void sm();
    descriptor: ()V
    flags: ACC_SYNCHRONIZED
    Code:
      stack=0, locals=1, args_size=1
         0: return
      LineNumberTable:
        line 15: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       1     0  this   Lcom/mashibing/juc/c_025/SynchronizedClass;
	//m方法
  void m();
    descriptor: ()V
    flags:
    Code:
      stack=2, locals=4, args_size=1
         0: new           #2                  // class java/lang/Object
         3: dup
         4: invokespecial #1                  // Method java/lang/Object."<init>":()V
         7: astore_1
         8: aload_1
         9: dup
        10: astore_2
        11: monitorenter
        12: aload_2
        13: monitorexit
        14: goto          22
        17: astore_3
        18: aload_2
        19: monitorexit
        20: aload_3
        21: athrow
        22: return     
   	//省略部分代码....       
}
```

JVM基于`enter`和`exit`对象来实现同步方法和代码块（JVM规范），只是实现细节有所不同。在上面编译完的`class`字节码中， 同步方法通过`ACC_SYNCHRONIZED` 实现（第7行和第14行）。代码块同步通过monitorenter（第38行）和2个monitorexit（40行和44行）实现，其中第二个monitorexit是在方法异常的时候退出monitor。可以把monitorenter和monitorexit理解成lock和unlock，分别插入到代码的开始和结束。每个对象都对应一个Monitor对象，当线程执行到monitorenter时，先检查Monitor是否为被持有，如果未被持有，则成为Monitor的拥有者。否则进入阻塞状态。

若monior的进入数为0，线程可以进入monitor，并将monitor的进入数置为1。当前线程成为monitor的owner（所有者）

若线程已拥有monitor的所有权，允许它重入monitor，并递增monitor的进入数

若其他线程已经占有monitor的所有权，那么当前尝试获取monitor的所有权的线程会被阻塞，直到monitor的进入数变为0，才能重新尝试获取monitor的所有权。

## 锁升级 JVM的实现

先简单了解操作系统的内核态和用户态。

* 内核态：运行操作系统程序，可以访问计算机硬件。
* 用户态：运行用户程序，无法访问计算机硬件。

JDK1.6之前，synchronized被称作重量级锁，是因为每次加锁都需要向操作系统申请指令，进入内核态。

打怪每次都放大招，非常消耗能量。聪明的玩家及时调整了策略，打小怪用小招，打大怪用大招。

聪明的开发者对synchronized也做了改进，原则是能在用户态加锁就不到内核态加锁。他们给锁分了等级，从低到高分别

无锁、偏向锁、轻量级锁、重量级锁。它们随着竞争的激烈不断升级。锁只能升级不能降级。偏向锁和轻量级锁在用户态完成，重量级锁在内核态完成。

### 对象头

这些锁状态被记录在对象头的Mark Word中，同时它还存储了对象运行时数据，比如GC分段年龄、锁状态、HashCode等。

下面分别介绍这几种锁

* 偏向锁

  大多数时间并没有多线程竞争锁，而且获取到锁的总是同一线程。为了提高线程获取锁的效率引入了偏向锁。当有线程访问锁的时候，通过CAS的方式把对象头中偏向锁对应的线程ID修改成该线程的ID。再次访问只需检查偏向锁对应的ID是否为自己的ID的即可。如果是则表示获取锁成功。
  
  * 锁撤销，偏向锁并不是真正的上锁，它只是Mark Word中的一个标示。所以对线程的效率并没有影响，当有线程来竞争偏向锁的时候才会撤销偏向锁，重新竞争。

* 自旋锁

  当有了锁竞争，就会升级到自旋锁，抢不到锁的线程自旋抢，当自旋到一定数量的时候就升级为重量级锁。具体是自旋多少次，可以通过-XX:PreBlockSpin设置，默认10次，或者超过CPU核数的一半，1.6之后，加入了自适应自旋Adapative self spinning，jvm自己控制，不需要自己调。

* 重量级锁

  重量级锁是向操作系统申请锁，

可重入锁，锁几次必须记录，在markword，因为锁几次一定要解几次。

有自旋为何还要重量级锁。自旋是要占用cpu资源的，如果线程太多，cpu资源消耗太多会造成系统奔溃。

这时候升级为重量级锁 ，把这些线程放到waitset 队列里。等待系统调用，就不需要消耗系统资源。



偏向锁一定比自旋锁效率高吗？

不一定。在明确知道有多线程的情况下，偏向锁一定会涉及到锁撤销，消耗能量。

jvm启动时一定会启动很多线程。此时不打开偏向锁。过一小段时间再打开。



[参考](https://www.jianshu.com/p/c3313dcf2c23)



对比：

优缺点

| 锁       | 优点                                                         | 缺点                                   | 实用场景                                           |
| -------- | ------------------------------------------------------------ | -------------------------------------- | -------------------------------------------------- |
| 偏向锁   | 加锁和解锁不需要额外的系统开销，和非同步方法在效率上差别不大 | 如果存在锁竞争，会带来额外的锁撤销消耗 | 只有一个线程访问的同步块                           |
| 轻量级锁 | 竞争的线程不会阻塞，提高线程的响应速度                       | 如果始终获取不到锁。自旋会消耗cpu      | 追求响应时间，同步块执行时间比较短，执行线程比较少 |
| 重量级锁 | 竞争不实用自旋，不消耗cpu资源。只需在队列里等待系统调用。    | 线程阻塞，响应时间慢                   | 追求吞吐量，同步块执行比较慢，锁竞争激烈           |







