本次作业要求大家实现一个通用的CRC计算器，在CRC类中实现如下2个方法

1.

``` java
 public static char[] Calculate(char[] data, String polynomial)
```

根据数据流与多项式计算出校验码

2.

``` java
 public static char[] Check(char[] data, String polynomial, char[] CheckCode)
```

根据数据流、多项式与校验码计算出余数
