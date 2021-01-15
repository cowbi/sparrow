# 程序员需求都写不完，还有必要写javadoc吗

时间紧，任务重，这是程序员朋友们的常态。很多产品设计貌似没工期，测试的工期取决于开发交付时间和质量，但是老板要要要，刻不容缓，能压缩的只有开发的工期。而开发的质量看似又和写不写javadoc没什么关系。javadoc自然会被大家忽略。别说是javadoc，就是方法里的关键代码注释都不写，接口就更没文档了。未来的自己和当下的同事看那段代码犹如天书。心中无数次默念mgb。

为了可怜的同事和将来的自己，还是尽量写点javadoc，写点注释。如果你实在不写，我也不能打你，毕竟够不着。

其实不难发现，javadoc在一些优秀的源码中占据的篇幅并不少，更有甚者占整个文件的2/3。看完注释基本上知道该类或方法的作用，再结合代码看更是了然于胸。都说了优秀的源码写的注释比较多，如果你也写了很多有效注释，是不是也显得比较牛呢？

## 如何写javadoc

本文主要说明在写javadoc时的一些注意点及使用习惯。具体语法细节、如何生成javadoc不是本文重点，想了解相关内容的小伙伴可以看官方文档，比我权威「偷笑」。

[javadoc官方说明](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/javadoc.html)

[如何为Javadoc工具编写文档注释](https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html)

[编写Java API规范的要求](https://www.oracle.com/java/technologies/javase/api-specifications.html)

### javadoc一般格式

* 注释project

  项目介绍一般写在根目录下的README.md文件。md正在成为程序员的标配，走哪跟哪。

* 注释package

  在该包下新建一个`package-info.java`文件，它可以包含程序包声明，程序包注释。首先它是一个java文件，但又不是普通的java文件，它的名字中有 `-` ,这在javaclass命名中是非法的。所以一般人也创建不了它。

  ```java
  /** 
   * <b>package-info不是平常类，其作用有三个:</b><br> 
   * 这个类就是这么另类，没有类名，因为类名非法。所以也不会有继承实现。
   * 所有属性的修饰符都是default，因为它是说明该包的。
   * 
  */  
  package com.package;
  //包常量，只允许包内访问
  class PkgConstant{
      static final String PACKAKGE_CONST="GOOD";
  }
  ```

* 注释class

  以我们比较痛恨的HashMap为例，下面是部分代码&注释。

  ```java
  /*
   * Copyright (c) 1997, 2017, Oracle and/or its affiliates. All rights reserved.
   * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
   */
  
  package java.util;
  
  import java.io.IOException;
  import java.io.InvalidObjectException;
  import java.io.Serializable;
  ......
  
  /**
   * Hash table based implementation of the <tt>Map</tt> interface.  This
   * implementation provides all of the optional map operations, and permits
   * <tt>null</tt> values and the <tt>null</tt> key.  (The <tt>HashMap</tt>
   * class is roughly equivalent to <tt>Hashtable</tt>, except that it is
   * unsynchronized and permits nulls.)  This class makes no guarantees as to
   * the order of the map; in particular, it does not guarantee that the order
   * will remain constant over time.
   *
   * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
   * as it is, generally speaking, impossible to make any hard guarantees in the
   * presence of unsynchronized concurrent modification.  Fail-fast iterators
   * throw <tt>ConcurrentModificationException</tt> on a best-effort basis.
   * Therefore, it would be wrong to write a program that depended on this
   * exception for its correctness: <i>the fail-fast behavior of iterators
   * should be used only to detect bugs.</i>
   *
   * <p>This class is a member of the
   * <a href="{@docRoot}/../technotes/guides/collections/index.html">
   * Java Collections Framework</a>.
   *
   * @param <K> the type of keys maintained by this map
   * @param <V> the type of mapped values
   *
   * @author  Doug Lea
   * @author  Josh Bloch
   * @author  Arthur van Hoff
   * @author  Neal Gafter
   * @see     Object#hashCode()
   * @see     Collection
   * @see     Map
   * @see     TreeMap
   * @see     Hashtable
   * @since   1.2
   */
  public class HashMap<K,V> extends AbstractMap<K,V>
      implements Map<K,V>, Cloneable, Serializable {
    ......
  }
  ```

  1. 最上面声明该class所属公司，版权，许可等。

  2. 对class总体介绍，比如基于Hashtable，可以存null值等。

  3. 对class比较详尽的介绍

  4. 作者、日期、版本等信息

* 注释Method

  看一下HashMap#put方法

  ```java
	/**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true); //调用putVal
    }

  ```

  1. 方法介绍
  2. @param 参数介绍
  3. @return 返回值介绍
  4. 有时方法作者和本类作者不一致，还要写上作者，日期等。
  5. 方法内代码行注释用`//`就可以

* 注释 Field

比较简单，解释清楚就好

  ```java
    /**
     * The default initial capacity - MUST be a power of two.
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
  ```

### 常见文档标记

列举一些常见，但又不是很清楚的文档标记，像@author、@param、@return、@version这些就不必赘述了。

* `@see`  参考，引用。后面可以是链接或者文本。文档中可以有多个@see，可以出现在类上或方法上。下面是它的3种常见形式：

  1. **`@see`** "thing" : thing是一个无法提供链接的字符串，可以是一本书或者一个定义，或者其他什么。javadoc以双“”号来识别thing。例如`@see “金瓶梅”`
  2. **`@see`** `<a href="`URL#value`">`label`</a>` ：可以跳转到一个url，url      可以是绝对路径或相对路径。
     例如:

     ```java
   / **
      * @see <a href="jpm.html#section">Java 金瓶梅</a>
      */
     ```
  3. **`@see`** `package.class#member label` ，引用有效的类或方法，也是最常用到的形式。比如：

     ```java
      / ** 
      * @see String＃equals（Object）等于
      * /
     ```

* **`{@link`** package.class`#`member label**`}`**，指向包、类、类成员。用法和@see类似，主要区别在于 `{@link}`生成的是嵌入式链接，而不是将链接放在“另请参见”部分中，@link只是一个超链接，而@see具有参考，借鉴作用。用法示例：

  ```java
  /** 
  *  {@link Collections#synchronizedMap Collections.synchronizedMap}
  *  {@link ConcurrentModificationException} 已经引入包，无需再写包路径，直接写类即可。
  */
  ```

* **`{@code`** text**`}`**：将文本标记为代码样式的文本，无需解析。javadoc最终生成的是html，如果注释中有html可以解析的代码就会被解析，这当然不是我们想要的。比如：

  ```java
   / **
     * {@code <b>} 如果不申明为code，javadoc会解析成换行。
     */
  ```

* **`{@value`** package.class#field**`}`**，表示常量值。有2种情况，

  1. 不带参数，如：

     ```java
        /**
          * The value of this constant is {@value}.
          */
         public static final String MY_CONSTANT = "GOOD"
     ```

  2. 带参数package.class#field，和@see、@link的参数一样，只是这里的field必须是常量。如

     ```java
           /**
             * Evaluates the script starting with {@value #SCRIPT_START}.
             */
            public String evalScript(String script) {
            
     ```

* html语法

  javadoc可以识别注释中的html代码。常见的有:

  ```
  <p> <pre> <tt> <a> <ul>、<i> ......
  ```

## 总结

在编程过程中其实有很多可写可不写的细节，比如代码洁癖，比如编程规范，比如写注释。细节处才能看出一个人的真正水平。细节往往决定成败。加油少年，做一个有追求的码农，快快乐乐搬砖。记得点赞再走。