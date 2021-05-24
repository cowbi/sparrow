package bridge;

/**
 * 桥梁模式，代替继承，业务类的属性是另一个类. 用组装代替继承。
 *
 * 多个业务角度组合，比如球，可以通过颜色和大小来分，那就有一个颜色的抽象类，一个大小的抽象类。其中颜色抽象类引入大小的抽象类，
 * 桥接到一起就决定了一个球。
 *
 * 继承的优点，公共的方法抽取，父类抽象，子类实现。
 * 缺点就是强关联。试想一种场景：father类，son类继承类father类。grandSon继承son。
 * 这时候son想重写继承过来的方法是有风险的。对grandSon造成重大影响。
 *
 * 如果使用桥接模式，就不会发生上面的情况。
 *
 * 如果明确不发生变化，使用继承。如果不确定就使用桥梁。
 *
 * 工厂和产品分开
 *
 * @author zhaoyancheng
 * @date 2021-05-22 23:05
 **/
public class ClientMain {

    public static void main(String[] args) {

        AbstractProduct houseProduct = new HouseProduct();
        //换一个房子
        houseProduct = new House2Product();
        //再换其他类型的房子也是可以的，只需要改其中的实现类。
        AbstractCorp corp = new HouseCorp(houseProduct);
        corp.getMoney();


        //山寨公司也想生产房子,也可以。
        ShanzaiCorp shanzaiCorp = new ShanzaiCorp(houseProduct);
        shanzaiCorp.getMoney();



    }
}
