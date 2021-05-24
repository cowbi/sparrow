package factory.factoryMethod;

/**
 * @author zhaoyancheng
 * @date 2021-05-18 22:28
 **/
public class CarFactory {

    public Car create() {
        //before processing
        return new Car();
        //after processing
    }
}
