# ReentrantLock

```java
try {
      lock.lock(); //synchronized(this)
      for (int i = 0; i < 10; i++) {
         TimeUnit.SECONDS.sleep(1);

         System.out.println(i);
      }
   } catch (InterruptedException e) {
      e.printStackTrace();
   } finally {
      lock.unlock();
   }
}
```

ReenTrantLock可以替代 synchronized。 一定要写在try->lock.lock  finally->lock.unlock里。



## ReentrantLock比synchronized 强大的地方

1. trylock，灵活

   ```
   /**
    * 使用tryLock进行尝试锁定，不管锁定与否，方法都将继续执行
    * 可以根据tryLock的返回值来判定是否锁定
    * 也可以指定tryLock的时间，由于tryLock(time)抛出异常，所以要注意unclock的处理，必须放到finally中
    */
   void m2() {
      /*
      boolean locked = lock.tryLock();
      System.out.println("m2 ..." + locked);
      if(locked) lock.unlock();
      */
      
      boolean locked = false;
      
      try {
         locked = lock.tryLock(5, TimeUnit.SECONDS);
         System.out.println("m2 ..." + locked);
      } catch (InterruptedException e) {
         e.printStackTrace();
      } finally {
         if(locked) lock.unlock();
      }
      
   }
   ```

2. lockInterruptibly

```java
Thread t1 = new Thread(()->{
      try {
         lock.lock();
         System.out.println("t1 start");
         TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
         System.out.println("t1 end");
      } catch (InterruptedException e) {
         System.out.println("interrupted!");
      } finally {
         lock.unlock();
      }
   });
   t1.start();
   
   Thread t2 = new Thread(()->{
      try {
         //lock.lock();
         lock.lockInterruptibly(); //可以对interrupt()方法做出响应
         System.out.println("t2 start");
         TimeUnit.SECONDS.sleep(5);
         System.out.println("t2 end");
      } catch (InterruptedException e) {
         System.out.println("interrupted!");
      } finally {
         lock.unlock();
      }
   });
   t2.start();
   
   try {
      TimeUnit.SECONDS.sleep(1);
   } catch (InterruptedException e) {
      e.printStackTrace();
   }
   t2.interrupt(); //打断线程2的等待
   
}
```

3. New ReentrantLock 可以是公平锁。

   公平锁：排队，先到的线程先获得锁。

   非公平锁：优先级反转、饥饿现象。优点是吞吐量比公平锁大。

4. cas vs synchronized升级