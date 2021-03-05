## 简介
本文主要针对的读者是有Eureka实际开发经验和面试遇到Eureka相关问题不知道怎么回答的小伙伴，下面直接列举出关于Eureka若干点，每一个点都是重点，值得深究。
### 1. Eureka的自我保护
服务注册到Eureka中以后。默认情况下每30s（默认）给Eureka发一次心跳，如果Eureka一段时间（默认90s）没收到心跳就会把该服务剔除。
但是有时候服务正常，只是由于网络异常抖动没有把心跳发送至Eureka，如果Eureka这时把服务剔除，当网络恢复正常时，服务也不会重新注册到Eureka（服务只有在启动的时候才注册Eureka）。服务通过Eureka是访问不了的。

为了防止这种误杀，Eureka提供了自我保护机制：Eureka在15分钟内收到服务端心跳数小于Eureka本应该收到的总心跳数 * 自我保护阈值（默认0.85）就会触发。该机制默认开启。等网络恢复后退出自我保护。

**总体思想就是：宁可保住不健康的，也不盲目注销任何健康的服务。**

比如我们有10台服务器，正常情况下15分钟应该给Eureka发送10 * （2 * 15 ） = 300 次心跳（30秒一次），但此时Eureka收到的心跳小于300 * 0.85 = 255，就会触发自我保护。不发心跳有2种可能。
1. 服务没挂，网络等原因，Eureka没收到服务心跳。网络恢复后继续发送心跳。
2. 服务挂了，来不及下线。那什么时候才从注册列表移除？就是等那些真的因为网络异常抖动保护起来的服务重新发送心跳。

* 那什么时候开自我保护什么时候不开呢？
1. 我个人一点点小思考，抛砖引玉，服务多的开保护，服务少的不保护。
因为服务多的时候，超过15%没收到心跳，网络问题可能性更大。但是服务少超过15%没心跳，服务挂的可能性更大，如果把挂掉的服务保护起来，就会给客户端返回错
2. 当然为了保证线上系统的健壮稳定，可以在任何情况下开启自我保护。

自我保护配置如下：
```
eureka:
  server:
      ## 自我保护
      enable-self-preservation: true
      ## 自我保护触发的阈值，可以适当修改
      renewal-percent-threshold: 0.85
```
### 2. 快速下线
Eureka Server在启动时会创建一个定时任务，每隔一段时间（默认60秒），从当前服务清单中把超时没有续约（默认90秒）的服务剔除。我们可以把定时任务间隔的时间设置的短一点，做到快速下线。防止拉取到不可用的服务。
```
eureka:
  server:
     eviction-interval-timer-in-ms: 3000 //比如3s
```
### 3. 缓存优化
Eureka Server为了避免同时读写内存数据结构造成的并发冲突问题，采用了3级缓存机制来进一步提升服务请求的响应速度。
拉取注册表的步骤是：
1. 首先从ReadOnlyCacheMap里查缓存的注册表。
2. 若没有，就找ReadWriteCacheMap里缓存的注册表。
3. 如果还没有，就从内存中获取实际的注册表数据。

当注册表发生变化的时候，先更新注册表数据和ReadWriteCacheMap里缓存的数据，默认30s后把ReadWriteCacheMap里面的数据更新到ReadOnlyCacheMap。
为了提高服务被发现的速度。我们可以做一些设置。
1. 拉去服务的时候，不从ReadOnlyCacheMap里查，直接从ReadWriteCacheMap取。
```
eureka:
  server:
	use-read-only-response-cache: false //关闭从ReadOnlyCacheMap拉取数据。
```
2. 缩短ReadWriteCacheMap向ReadOnlyCacheMap同步的时间间隔，默认30秒，我们可以优化到3秒，这个根据自己的情况而定。
```
eureka:
  server:
	response-cache-update-interval-ms: 3000
```
这里看源码的时候发现代码有个问题：
```
if (shouldUseReadOnlyResponseCache) {
            timer.schedule(getCacheUpdateTask(),
                    new Date(((System.currentTimeMillis() / responseCacheUpdateIntervalMs) * responseCacheUpdateIntervalMs)
                            + responseCacheUpdateIntervalMs),
                    responseCacheUpdateIntervalMs);
        }
```
为什么System.currentTimeMillis() 除以 responseCacheUpdateIntervalMs 又乘responseCacheUpdateIntervalMs，这不还是原来的System.currentTimeMillis() 吗？

其实用timer也是存在隐患的，就是多线程并行处理定时任务时，timer运行多个timetask时，只要其中之一没有捕获抛出的异常，其他任务便会自动终止运行。可以改成用ScheduledExcutorService。

### 4. 客户端开发小技巧
我们开发客户端的时候，如果不启动注册中心就会一直报注册中心链接超时的错。我们可以在开发的时候做如下配置，让服务和注册中心脱钩。
```
eureka:
  client:
    ### 不拉取也不注册、跟注册中心没关系
    enabled: false
```
### 5. 客户端拉取注册表更即时
api-client会定时到eureka-server拉取注册表。默认情况下每30秒拉取一次。可以根据实际情况设置拉取时间间隔。
```
eureka:
  client:
   fetch-registry=true
   ### 拉取注册表信息间隔时
   registry-fetch-interval-seconds: 3
```
### 6. client.serviceUrl.defaultZone优化
api-client从eureka-server拉取注册表信息是按照defaultZone配置的顺序依次拉取的，当eureka1不可用的时候再从eureka2中获取/注册。但是如果eureka1一直不挂。所有的微服务都会先从eureka1中获取信息，导致eureka1压力过大。在实际生产中，每个微服务可以随机配置不同的defaultZone顺序。手动做到负载均衡。比如clientA的defaultZone:是eureka1,eureka2,eureka3；clientB的defaultZone:是eureka2,eureka3,eureka1。
```
eureka:
  client:
    serviceUrl:
      defaultZone: eureka1,eureka2,eureka3
```
### 7. client心跳频率
默认情况下，client每隔30秒就会向服务端发送一次心跳。这个时间也可以适当调小一点。
```
eureka:
 instance:
    ##每间隔30s，向服务端发送一次心跳，证明自己依然”存活“。
    lease-renewal-interval-in-seconds: 30
```
### 8. 服务端剔除客户端的时间间隔
默认情况下，server在90s之内没有收到client心跳，将我踢出掉。为了让服务快速响应，可以适当的把这个时间改的小一点。
```
eureka:
 instance:
    lease-expiration-duration-in-seconds: 90
```


## Eureka其他问题

### 在哪些地方没实现一致性？也就是CAP中的C。
1. 自我保护机制，使网络不好的情况下还会能拉取到注册表进行调用。
2. 在缓存同步的时候没实现。上面我们在优化缓存的时候发现，ReadOnlyCacheMap和ReadWriteCacheMap之间的数据没实现一致性。
3. 从其他peer拉取注册表。集群之间的状态是采用异步方式同步的，所以不保证节点间的状态一定是一致的，不过基本能保证最终状态是一致的。

### 集群同步，集群并没有扩大Eureka并没有扩大它的承受能力，只是实现了可用性。
在什么情况下会同步数据？我们从以下几个节点分析。
1. 注册：第一个节点注册进来，只同步下一个节点。
2. 续约：有新服务续约，自动同步到其他Eureka-Server。
3. 下线：一直同步所有集群。
4. 剔除：不同步，每个Server都有自己的剔除机制。

### 估算能承受多少服务量

比如有20个服务，每个服务部署5个实例。就是20 * 5 = 100实例。

1. 一个实例默认30秒发一次心跳，30秒拉取一次注册表。那Service每分钟接收到的请求量就是。100 * 2 * 2 = 400次。那一天能承受的量就是 400 * 60 * 24 = 576000次请求。也就是每天500多万的访问量。

所以通过设置一个适当的拉取注册表以及发送心跳的频率，可以保证大规模系统里对Eureka Server的请求压力不会太大。

### 生产中的问题，当重启服务的时候，还是可以访问，但是返回服务错误
在服务启动时，一定要先停服，再手动触发下线。
如果不手动下线，可能会访问到重启中的服务。而这个服务不可用。
如果先手动下线，可能还会拉取到重启的服务，手动下线无效。

### 区域问题
当用户量比较大的时候，我们服务可能布置到不同区域、不同机房。如果我们上线微服务的时候，希望同一机房的服务调用同一机房的服务，当同一机房的服务不可用在调用其他机房的服务。类似CDN吧。这样可以减少网络延迟。
eureka提供2个概念来分区。
1. region：相当于地区，比如北京地区。
2. zone：是region下属单位，比如北京甲机房、乙机房。

## 总结
就像开头所说，我们分析并解答了Eureka开发、线上部署、面试遇到的哪些棘手问题。相信对你有所帮助。
当然本人水平有限，能力一般，是个IT界的小学生。有什么问题大家在留言区提出来，一起讨论。
互相促进是我最大的快乐。