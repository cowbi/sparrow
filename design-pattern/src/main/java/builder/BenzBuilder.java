package builder;

import java.util.ArrayList;

/**
 * @author zhaoyancheng
 * @date 2021-05-20 23:10
 **/
public class BenzBuilder extends CarBuilder {

    BenzCar car = new BenzCar();

    @Override
    BenzCar getCarModel() {
        return this.car;
    }

    @Override
    void setSequence(ArrayList<String> sequence) {
        if (sequence.get(0).equals("run")){
            this.car.watch();
        } else {
            this.car.run();
        }
    }
}
