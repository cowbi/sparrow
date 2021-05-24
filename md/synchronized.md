* synchronized

* cas 汇编命令



* jvm 内存布局



* 0x80执行过程- 用户态到内核态





* 线程6种状态

1. new 线程刚刚创建，还没启动
2. runnable ，可运行状态，由线程调度器可安排执行，又可分为 ready-准备就绪 和 running--正在运行
3. waiting，等待被唤醒
4. timed waiting，等待一段时间后被唤醒
5. blocked，被阻塞，正在等待锁
6. terminated，线程结束
7. ![image-20210204233355899](/Users/zhaoyancheng/Library/Application Support/typora-user-images/image-20210204233355899.png)

