package strategy;

/**
 * 策略模式 英[ˈstrætədʒi]
 *
 * 要素：
 * 1.策略接口
 * 2.实际策略
 * 3.策略使用者
 *
 * 策略（Strategy）模式的定义：该模式定义了一系列算法，并将每个算法封装起来，使它们可以相互替换，且算法的变化不会影响使用算法的客户。
 * 通过对算法进行封装，把使用算法的责任和算法的实现分割开来，并委派给不同的对象对这些算法进行管理。
 *
 *
 * 实际应用：
 * Comparable && Comparator
 *
 * @author zhaoyancheng
 * @date 2021-05-19 11:42
 **/
public class ClientMain {

    public static void main(String[] args) {
        SilkBag silkBag = new SilkBag(new Kong());
        silkBag.run();

        SilkBag silkBag2 = new SilkBag(new Run());
        silkBag2.run();
    }

}
