# COA2021-programming11

**Good luck and have fun!**



## 1 实验要求

### 1.1 MMU

将cache与TLB融合到MMU中。

### 1.2 TLB

在TLB中添加你需要使用到的方法。



## 2 实验攻略

### 2.1 实验概述

本次实验为虚拟存储器的模拟。在上次作业中，大家已经实现了地址转换与数据加载的功能，本次作业主要的任务将cache、TLB、memory、disk融合成一个完整的存储器系统。

请认真阅读课本和PPT，确保你已经理解存储器系统中的每个部件是如何产生联系的。接下来的工作将会把编程作业7到11统统联系起来，相信你完成之后将会非常有成就感！



### 2.2 代码导读

#### 2.2.1 代码结构

```bash
.
│  .gitignore
│  2021.iml
│  pom.xml
│  README.md
│
└─src
    ├─main
    │  └─java
    │      ├─cpu
    │      │  ├─alu
    │      │  │
    │      │  ├─fpu
    │      │  │
    │      │  ├─mmu
    │      │  │      MMU.java	// need to write
    │      │  │
    │      │  └─nbcdu
    │      │
    │      ├─memory
    │      │  │  Memory.java	// need to write
    │      │  │
    │      │  ├─cache
    │      │  │
    │      │  ├─disk
    │      │  │
    │      │  └─tlb
    │      │          TLB.java	// need to write
    │      │
    │      └─util
    │
    └─test
        └─java
            └─memory
                    CacheTest.java
                    EasterEgg.java
                    MemTestHelper.java
                    PSTest.java
                    TLBTest.java

```

#### 2.2.2 TLB的模拟

我们模拟了一个大小为256行、使用全相联映射、FIFO替换策略的TLB。TLB类的源码比较简单，相关注释也非常详细，大家可以自行阅读。

需要特别提醒的是，TLB只是页表的缓存，因此TLB行号并不等于虚页号。

#### 2.2.3 MMU的改动

在上次作业中，我们默认cache与TLB都是关闭的，MMU的所有访存操作都是直接对主存进行操作。

而本次作业中，我们在MMU中加入了对全局变量`Cache.isAvailable`和`TLB.isAvailable`的判断。这两个全局变量分别表示是否启用cache与TLB。我们提供的代码（即上次作业的框架代码）里面已经处理好cache和TLB都关闭的情况，至于cache和TLB都开启的情况就需要大家动手去实现啦！



### 2.3 实现指导

#### 2.3.1 cache的融合

第一步，你需要将cache融合进MMU中。

这一步相对简单。需要注意，由于cache是memory的缓存，所以任何涉及到访问主存数据的地方都要添加对cache的调用。

#### 2.3.2 TLB的融合

第二步，你需要将TLB融合进MMU中。

这一步相对麻烦。即使填写TLB的方法我们已经帮助大家实现好了，但从TLB中读取数据的方法仍然需要大家自行设计并进行调用。由于TLB是页表的缓存，所以任何涉及到访问页表的地方都要添加对TLB的调用。下面作一点提示。

在addressTranslation方法中判断是否缺页并进行加载页的时候，原代码如下：

```java
if (!memory.isValidPage(i)) {
    // 缺页中断，该页不在内存中，内存从磁盘加载该页的数据
    memory.page_load(i);
}
```

在启用TLB之后，需要改动的地方有：

1. 启用TLB之后，判断是否缺页的工作应该首先交给TLB来完成，这时候就需要用到你自己设计的TLB的方法。
2. 如果发生缺页，page_load方法会进行填页表，填页表之后不要忘记填TLB。
3. 如果发生了虚页不在TLB但在页表中的情况，即TLB缺失但页表命中的情况，这个时候需要把页表中的该虚页项填到TLB中，我们有相关的测试用例来专门测试这种特殊情况。

同时，在上次作业中你自己实现的toPagePhysicalAddr方法里面，从页表中取物理页框号的时候，也应该改成从TLB中取物理页框号，这个时候也需要调用你自己设计的TLB的方法。参考结构如下

```java
if (TLB.isAvailable) {
	访问TLB取物理页框号;
} else {
	访问页表取物理页框号;
}
```



### 2.4 总结

所有需要大家完成的部分都已经用TODO标出。为了减轻大家的负担，我们归纳了本次作业中你需要完成的小任务以及步骤

1. 将上次作业的代码复制过来，然后你应该可以通过PSTest（关闭cache和TLB的情况）的4个用例。（粘贴代码的时候注意不要整个文件粘贴过来，否则会破坏框架代码的改动）
2. 正确实现好cache的融合，然后你应该可以通过CacheTest（开启cache关闭TLB的情况）的1个用例。
3. 正确实现好TLB的融合，然后你应该可以通过剩余5个用例（开启cache和TLB的情况）。

至此，你已经完成了全部工作(・ω・)ノ



### 2.5 彩蛋

作为存储模块的最终章，我们设计了一个彩蛋供大家体会各个内存部件之间的关系。

细心观察的同学可以发现，我们在Memory类中新增了一个timer字段，在这个字段开启设为true后，所有访问内存以及访问页表的操作都将产生10毫秒的延时，而访问cache与TLB的操作则不会产生延时。由于在真实的计算机系统中，访问cache和TLB的速度要比访问主存的速度快得多。因此，我们设计了这个10毫秒的延时，来模拟这个速度上的差异。

在我们提供的测试用例中有一个EasterEgg类，这个类将不断的访问同一段数据，来模拟计算机的时间局部性原理。在EasterEgg类中的init方法里面有如下代码：

```java
Memory.timer = true;

Cache.isAvailable = true;
TLB.isAvailable = true;
```

大家可以修改两个isAvailable字段，自主设置cache和TLB是否启用，来观察他们速度上的差异。以下是我自己的运行结果，仅供参考，我的处理器型号是Intel(R) Core(TM) i7-8565U CPU @ 1.80GHz   1.99 GHz。

| 运行时间  | 启用cache | 不启用cache |
| --------- | --------- | ----------- |
| 启用TLB   | 约100ms   | 约4.75s     |
| 不启用TLB | 约9.5s    | 约14.25s    |

大家可以尽情修改彩蛋，比如修改延时的时间、修改cache和tlb是否启用等。也可以在彩蛋里面单步进入，看看从mmu开始是怎么一步步访问disk、memory、TLB和cache的。

最后，留给大家一个问题，为什么在我们的模拟下，单独关闭cache和单独关闭TLB的运行时间差那么远？

(・ω・)ノ