package builder;

/**
 * 建造者（Builder）模式的定义：指将一个复杂对象的构造与它的表示分离，使同样的构建过程可以创建不同的表示，
 * 这样的设计模式被称为建造者模式。它是将一个复杂的对象分解为多个简单的对象，
 * 然后一步一步构建而成。它将变与不变相分离，即产品的组成部分是不变的，但每一部分是可以灵活选择的。
 *
 * 角色：
 * 1.抽象产品
 * 2.具体产品
 * 3.抽象构建者
 * 4.具体构建者
 * 5.导演类
 *
 *
 * 使用场景：产品非常复杂 产品中调用顺序不同可能导致不同的产品
 *
 * 一个产品的定价计算模型有 N 多
 * 种，每个模型有固定的计算步骤，计算非常复杂，项目中就使用了建造者模式；
 *
 *
 * @author zhaoyancheng
 * @date 2021-05-20 22:41
 **/
public class ClientMain {

    public static void main(String[] args) {
        Director director = new Director();

        //100辆奔驰
        for (int i = 0; i < 100; i++) {
            director.getBenz();
        }
    }
}
