package builder;

import java.util.ArrayList;

/**
 * @author zhaoyancheng
 * @date 2021-05-20 23:04
 **/
public class BmwBuilder extends CarBuilder{

    private BmwCar car = new BmwCar();

    @Override
    CarModel getCarModel() {
        return this.car;
    }

    @Override
    void setSequence(ArrayList<String> sequence) {

        if (sequence.get(0).equals("run")){
            this.car.run();
        }else {
            this.car.watch();
        }
    }
}
