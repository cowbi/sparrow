package builder;

import java.util.ArrayList;

/**
 * @author zhaoyancheng
 * @date 2021-05-20 23:02
 **/
public abstract class CarBuilder {

    abstract CarModel getCarModel();

    abstract void setSequence(ArrayList<String> sequence);
}
