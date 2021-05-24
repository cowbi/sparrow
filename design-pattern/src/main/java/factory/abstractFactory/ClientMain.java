package factory.abstractFactory;


/**
 * 抽象工厂的关键在于「族」。
 *
 * 1. 任意定制交通工具
 *    implements movable
 * 2. 任意定制生产过程
 *    **factory
 * 3. 任意定制产品一族
 * @author zhaoyancheng
 * @date 2021-05-18 22:27
 **/
public class ClientMain {

    public static void main(String[] args) {

        //如果需要换产品族，只需要更换具体的factory。
        AbstranctFactory factory = new ManFactory();

        Vehicle vehicle = factory.createVehicle();
        vehicle.run();

        Food food = factory.createFood();
        food.printName();

    }
}
