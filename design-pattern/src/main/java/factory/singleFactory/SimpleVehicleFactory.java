package factory.singleFactory;

import factory.factoryMethod.Bike;
import factory.factoryMethod.Car;

/**
 * 简单工厂
 * 可扩展性不好
 * @author zhaoyancheng
 * @date 2021-05-18 22:36
 **/
public class SimpleVehicleFactory {

    public Car createCar(){
        //before processing
        return new Car();
        //after processing
    }

    public Bike createBike(){
        //before processing
        return new Bike();
        //after processing
    }

}
