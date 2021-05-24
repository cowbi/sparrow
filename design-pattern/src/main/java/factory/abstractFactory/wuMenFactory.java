package factory.abstractFactory;

/**
 * @author zhaoyancheng
 * @date 2021-05-19 08:30
 **/
public class wuMenFactory extends AbstranctFactory{


    @Override
    Vehicle createVehicle() {
        return new Car();
    }

    @Override
    Food createFood() {
        return new Bread();
    }
}
