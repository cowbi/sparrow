# 作为一个老程序员，我从来不用Java8新特征

Oracle于2014发布了Java8（jdk1.8），诸多原因使它成为目前市场上使用最多的jdk版本，学习其新特征也是必要之举。虽然发布距今已将近7年，但很多程序员对其新特征还是不够了解，尤其是用惯了java8之前版本的老程序员，比如我。所以我们有必要对这些新特征做一些总结梳理。它较jdk.7有很多变化或者说是优化，比如interface里可以有静态方法，并且可以有方法体，这一点就颠覆了之前的认知；再比如我们过去可能不注意`java.util.Date`类是线程不安全的；`java.util.HashMap`数据结构里增加了红黑树；还有众所周知的Lambda表达式等等。本文不会把所以的新特征都给大家一一介绍，只拿出比较常用或好玩的10大特征给大家做详细讲解。[更多的新特征看官网](https://www.oracle.com/java/technologies/javase/8-whats-new.html)

## interface

interface的设计初衷是面向抽象，提高扩展性。这也留有一点遗憾，Interface修改的时候，实现它的类也必须跟着改。为了解决接口的修改与现有的实现不兼容的问题。新interface的方法可以用`default` 或 `static`修饰，这样就可以有方法体，实现类也不必重写此方法。

一个interface中可以有多个方法被它们修饰，这2个修饰符的区别主要也是普通方法和静态方法的区别。

1. `default`修饰的方法，是普通实例方法，可以用`this`调用，可以被子类继承、重写。
2. `static`修饰的方法，使用上和一般类静态方法一样。但它不能被子类继承，只能用`Interface`调用。

看一个接口代码

```java
public interface InterfaceNew {
    static void sm() {
        System.out.println("interface提供的方式实现");
    }
    static void sm2() {
        System.out.println("interface提供的方式实现");
    }
  
    default void def() {
        System.out.println("interface default方法");
    }
    default void def2() {
        System.out.println("interface default2方法");
    }
    //须要实现类重写
    void f();
}

public interface InterfaceNew1 {
    default void def() {
        System.out.println("InterfaceNew1 default方法");
    }
}
```

如果有一个类既实现了InterfaceNew又实现了InterfaceNew1，它们都有`def()`，这时就必须重写`def()`。

```java
public class InterfaceNewImpl implements InterfaceNew , InterfaceNew1{
    public static void main(String[] args) {
        InterfaceNewImpl interfaceNew = new InterfaceNewImpl();
        interfaceNew.def();
    }
    
    @Override
    public void def() {
        InterfaceNew1.super.def();
    }

    @Override
    public void f() {
    }
}
```

### interface & abstract class区别

既然interface也可以有自己的方法实现，似乎和abstract class没多大区别了。其实它们还是有区别的

1. interface和class的区别，好像是废话，主要有
   * 接口多实现，类单继承
   * 接口的方法是 public abstract修饰，变量是public static final修饰。 abstract class可以用其他修饰符
   
2. interface的方法是更像是一个扩展插件。而abstract class的方法是要继承的。

开始我们也提到，interface新增`default`，和`static`修饰的方法，为了解决接口的修改与现有的实现不兼容的问题，并不是为了要替代`abstract class`。在使用上，该用abstract class的地方还是要用abstract class，不要因为interface的新特征而降之替换。

**记住接口永远和类不一样。**

## Lambda 表达式

接下来谈众所周知的Lambda表达式。它是推动 Java 8 发布的最重要新特性。是继泛型(Generics)和注解(annotation)以来最大的变化。

使用 Lambda 表达式可以使代码变的更加简洁紧凑。让java也能支持简单的*函数式编程*。

> Lambda 表达式是一个匿名函数，java 8允许把函数作为参数传递进方法中。

### 语法格式

```java
(parameters) -> expression 或
(parameters) ->{ statements; }
```

### Lambda实战

我们用常用的实例来感受Lambda带来的便利

#### 替代匿名内部类

过去给方法传动态参数的唯一方法是使用内部类。比如

1. Runnable接口

```java
new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("The runable now is using!");
            }
}).start();
//用lambda
new Thread(() -> System.out.println("It's a lambda function!")).start();
```

2. Comperator接口

```java
List<Integer> strings = Arrays.asList(1, 2, 3);

Collections.sort(strings, new Comparator<Integer>() {
@Override
public int compare(Integer o1, Integer o2) {
    return o1 - o2;}
});

//Lambda
Collections.sort(strings, (Integer o1, Integer o2) -> o1 - o2);
//分解开
Comparator<Integer> comperator = (Integer o1, Integer o2) -> o1 - o2;
Collections.sort(strings, comperator);
```

3. Listener接口

```java
JButton button = new JButton();
button.addItemListener(new ItemListener() {
@Override
public void itemStateChanged(ItemEvent e) {
   e.getItem();
}
});
//lambda
button.addItemListener(e -> e.getItem());
```

4. 自定义接口

   上面的3个例子是我们在开发过程中最常见的，从中也能体会到Lambda带来的便捷与清爽。它只保留实际用到的代码，把无用代码全部省略。那它对接口有没有要求呢？我们发现这些匿名内部类只重写了接口的一个方法，当然也只有一个方法须要重写。这里需要引入一个概念：

   

   刚才涉及到的接口都属于函数式接口，只有函数式接口才能被lambda替代。很多函数式接口都有@FunctionalInterface注解，当然也有没有的。刚用到的Comparator、Runnable就有，而ItemListener没有。不管有没有都必须符合函数式接口的定义。@FunctionalInterface注解只是强制规定接口必须符合函数式接口的定义，否则会编译错误。java还提供了一个函数式编程的包`java.util.function`，该包下的所有接口都有@FunctionalInterface注解，也就是都可以用Lambda表达式。

   ```java
   @FunctionalInterface
   public interface Comparator<T>{}
   
   @FunctionalInterface
   public interface Runnable{}
   ```

   我们自定义一个函数式接口

   ```java
   @FunctionalInterface
   public interface LambdaFunctionalInterface {
       void f();
   }
   //使用
   public class LambdaClass {
       public static void forEg() {
           lambdaInterfaceDemo(()-> System.out.println("自定义函数式接口"));
       }
       //函数式接口参数
       static void lambdaInterfaceDemo(LambdaInterface i){
           System.out.println(i);
       }
   }
   ```

#### 集合迭代

```java
void lamndaFor() {
        List<String> strings = Arrays.asList("1", "2", "3");
        //传统foreach
        for (String s : strings) {
            System.out.println(s);
        }
        //Lambda foreach
        strings.forEach((s) -> System.out.println(s));
        //or
        strings.forEach(System.out::println);
 				//map
        Map<Integer, String> map = new HashMap<>();
        map.forEach((k,v)->System.out.println(v));
}
```

#### 方法的引用

Java 8允许使用 `::` 关键字来传递方法或者构造函数引用，无论如何，表达式返回的类型必须是functional-interface。

```java
public class LambdaClassSuper {
    LambdaInterface sf(){
        return null;
    }
}

public class LambdaClass {
    public static LambdaInterface staticF() {
        return null;
    }

    public LambdaInterface f() {
        return null;
    }

    void show() {
        //1.调用静态函数，返回类型必须是functional-interface
        LambdaInterface t = LambdaClass::staticF;

        //2.实例方法调用
        LambdaClass lambdaClass = new LambdaClass();
        LambdaInterface lambdaInterface = lambdaClass::f;

        //3.超类上的方法调用
        LambdaInterface superf = super::sf;

        //4. 构造方法调用
        LambdaInterface tt = LambdaClassSuper::new;
}
```

#### 访问变量

```java
int i = 0;
Collections.sort(strings, (Integer o1, Integer o2) -> o1 - i);
//i =3;
```

lambda表达式可以引用外边变量，但是该变量默认拥有final属性，不能被修改，如果修改，编译时就报错。

## StreamAPI

