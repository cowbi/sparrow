package builder;

import java.util.ArrayList;

/**
 * @author zhaoyancheng
 * @date 2021-05-20 23:17
 **/
public class Director {

    BenzBuilder benzBuilder = new BenzBuilder();
    BmwBuilder bmwBuilder = new BmwBuilder();

    public BmwCar getBmw() {
        ArrayList<String> sequence = new ArrayList<>();
        sequence.add("run");
        sequence.add("watch");
        bmwBuilder.setSequence(sequence);
        return (BmwCar) bmwBuilder.getCarModel();
    }

    public BenzCar getBenz() {
        ArrayList<String> sequence = new ArrayList<>();
        sequence.add("watch");
        sequence.add("run");
        benzBuilder.setSequence(sequence);
        return (BenzCar) benzBuilder.getCarModel();
    }
}
