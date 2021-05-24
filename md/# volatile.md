# volatile

## 作用

1. **禁止指令重排序**  

  double check lock 

   new 对象的3步
		1. 申请内存
		2. 初始化变量值
		3. 引用指向内存地址


2. **保证线程变量可见性**

   -锁总线
   
   -intel 的MESI（修改、独占、共享、无效）cpu缓存一致性协议

​    如何保证可见性： volatile转成汇编语言中有 lock #指令

lock 前缀的指令在多核处理器中保证2件事

1. 把当前处理器的缓存行写回到系统内存
2. 让其他cpu中缓存该内存地址的数据无效

[参考](https://www.cnblogs.com/dolphin0520/p/3920373.html)