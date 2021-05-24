package factory.abstractFactory;

/**
 * @author zhaoyancheng
 * @date 2021-05-19 08:30
 **/
public class ManFactory extends AbstranctFactory{


    @Override
    Vehicle createVehicle() {
        return new Bike();
    }

    @Override
    Food createFood() {
        return new Mantou();
    }
}
