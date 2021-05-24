package factory.factoryMethod;

/**
 * @author zhaoyancheng
 * @date 2021-05-18 22:28
 **/
public class BikeFactory {

    public Bike create() {
        //before processing
        return new Bike();
        //after processing
    }
}
