# Docker是如何让运维人员失业的



## 简介

2020疫情原因，公司业绩下滑严重，于是决定给团队瘦身，经过激烈的讨论，最终决定先从运维人员开始。原因是我们引入了Docker。它部署项目相比之前要简单许多。运维的工作完全可以被开发替代。当然我们小公司是这样的状况，大公司还是专人专责。最近也看到公司招聘程序员，熟悉Docker的优先录用。

### 为什么docker如此受欢迎？我们先看看它与传统的虚拟机有什么不同？

传统虚拟机是虚拟出一套硬件，拥有自己完整的操作系统，在上面可以跑需要的应用进程。
![vm](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e9bab24c265e44fd9d315bb3c599bbe3~tplv-k3u1fbpfcp-watermark.image)

这样有一些缺点，比如

启动慢，启动一个虚拟机得几分钟。
占用空间比较大，虚拟机的大小一般是几个G。我们真正想运行的是某些程序，而不是虚拟机，无奈程序是跑在虚拟机上的。只要把虚拟机带上。
资源消耗比较大，除了程序本身要消耗系统资源，虚拟机也很消耗资源。
如果我们能对虚拟机取其精华去其糟粕，岂不美哉。这个时候出现了容器（container）。Linux Container容器是一种内核虚拟化技术，可以提供轻量级的虚拟化，以便隔离进程和资源。Docker在其基础上进行了封装，提供更强大的功能。下图为Docder模型。
![](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/bc08b39c960041caba66a4e4d04f4df8~tplv-k3u1fbpfcp-watermark.image)
很多厂商都实现了容器技术，但由于Docker太流行，以至于很多时候用Docker指代容器。从上图可以看出，Docker实现了App级别的隔离，不像传统虚拟机是系统级别的隔离。每一个app都封装到容器里，直接运行在宿主机上，比运行在虚拟机上的程序更快。

在虚拟机的时候，我们每部署一个应用都需要把相应的参数，数据库配置等打包给运维人员。只要少一项就会出现大大小小的问题。但是Docker就不会出现这样的情况。只要文件打包好直接发给运维人员，可以实现一次打包，多处运行。

总之就是Docker能够将应用程序与基础架构分开，从而可以快速交付软件。

话不多说，我们先让Docker跑起来，爽完后再谈它的原理。

### Docker ：hello world！
* 安装yum-utils： 安装工具包，简化安装。
```
yum install -y yum-utils device-mapper-persistent-data lvm2
```
* 为yum源添加docker仓库位置：阿里云快点 -ce是开源的社区版本

```
yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
```
* 自动检查哪个源块
```
yum makecache fast
```
* 安装docker：
```
yum -y install docker-ce
```
* 启动docker：
```
service docker start
```
* 检查docker是否安装成功，出现正常版本号视为安装成功。

```
[root@VM-0-8-centos ~]# docker version
Client: Docker Engine - Community
 Version:           19.03.13
 API version:       1.40
 Go version:        go1.13.15
 Git commit:        4484c46d9d
 Built:             Wed Sep 16 17:03:45 2020
 OS/Arch:           linux/amd64
 Experimental:      false
Server: Docker Engine - Community
 Engine:
  Version:          19.03.13
  API version:      1.40 (minimum version 1.12)
  Go version:       go1.13.15
  Git commit:       4484c46d9d
  Built:            Wed Sep 16 17:02:21 2020
  OS/Arch:          linux/amd64
  Experimental:     false
 containerd:
  Version:          1.3.7
  GitCommit:        8fba4e9a7d01810a393d5d25a3621dc101981175
 runc:
  Version:          1.0.0-rc10
  GitCommit:        dc9208a3303feef5b3839f4323d9beb36df0a9dd
 docker-init:
  Version:          0.18.0
  GitCommit:        fec3683
```
* 检查docker是否能用，从远程仓库拉取镜像hello-word：docker pull hello-world
```
[root@VM-0-8-centos ~]# docker pull hello-world
Using default tag: latest
latest: Pulling from library/hello-world
0e03bdcc26d7: Pull complete 
Digest: sha256:8c5aeeb6a5f3ba4883347d3747a7249f491766ca1caa47e5da5dfcf6b9b717c0
Status: Downloaded newer image for hello-world:latest
docker.io/library/hello-world:latest
```
* 启动 hello-world：docker run hello-world
```
[root@VM-0-8-centos ~]# docker run hello-world
Hello from Docker!
This message shows that your installation appears to be working correctly.
To generate this message, Docker took the following steps:
 1. The Docker client contacted the Docker daemon.
 2. The Docker daemon pulled the "hello-world" image from the Docker Hub.
    (amd64)
 3. The Docker daemon created a new container from that image which runs the
    executable that produces the output you are currently reading.
 4. The Docker daemon streamed that output to the Docker client, which sent it
    to your terminal.
To try something more ambitious, you can run an Ubuntu container with:
 $ docker run -it ubuntu bash
Share images, automate workflows, and more with a free Docker ID:
 https://hub.docker.com/
For more examples and ideas, visit:
 https://docs.docker.com/get-started/
```
**到目前为止，已经有一个Docker容器hello-world就运行起来了。本文我们不具体讲Docker实操，只是简单体验一下Docker的温度。实操部分可以去看官网，或者等我下一篇文章的更新。**

### Docker是如何工作的
Docker采用的是常见的client-server模式。Client端负责接受用户输入的命令，像我们前面提到的docker pull、run等。真正执行命令的是Server端，也就是docker的守护线程daemon。client和daemon之间通过rest api进行通信，它们可以在同一台机器也可以在不同机器。类比mysql的client和server。
![](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/cf8fb30d71e74d7eb24185e69b96daf5~tplv-k3u1fbpfcp-watermark.image)
除了client和server，我们还看到Registry，它是储存Docker镜像的地方，类比maven的仓库。Docker Hub是官方提供的中央仓库，当然可以创建自己的私库。

* 这里我们有必要理解3个概念

1. image：镜像，可执行文件，就是在Docker Hub上存放的文件。就像我们windows系统里的exe文件，我们把它叫做程序。
2. container：是运行起来的实例，就是一个进程。image运行起来就是container。
3. dockerfile：就是image的源码。包括了依赖环境和Docker命令等。这里需要注意的是镜像是分层的，每一行指令都会在镜像中创建一层，当修改dockerfile重建时，只需要重建修改的那层就好。好比活字印刷，一页纸上要修改一个字，不需要重新换一个模版，只需把要修改的那个字替换了就行。
### Docker是如何实现隔离的
开始我们提到Docker是基于LXC实现的，本质上是宿主机上一个进程。

* 通过namespace实现资源隔离，我们知道系统中的pid、net、ipc、mnt、uts都是全局的。namespace使它们私有化，容器运行时，docker会为该容器创建一组namespace（分别对pid、net、ipc等创建自己的namespace），这样容器之间的资源互不干扰，好像一套独立的操作系统。
* 通过cgroup（Control groups）实现资源限制。虽然NameSpace实现了资源隔离，但还是可以不受限制的访问内存、CPU等。cgroup允许Docker将可用的硬件资源共享给容器，并有选择地实施限制和约束。比如可以限制特定容器可用的内存。
### 我们回顾一下这次分享的内容：

* 容器和虚拟机的区别
* 运行Docker并实现hello world。
* Docker是如何工作的
* Docker是如何实现隔离的

## 总结
回到标题，为什么Docker让运维人员雪上加霜呢？当然也是对一部分人而言。疫情期间，本身裁员就很严重。再加上Docker简单的部署，让很多运维人员失业，尤其在小公司，疫情期间第一批被裁掉的就是他们。技术的革新一定会牺牲一部分的利益。当人工智能真正来临的时候，你我的工作还保得住吗？让我们拭目以待。