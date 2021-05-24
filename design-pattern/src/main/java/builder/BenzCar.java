package builder;

/**
 * @author zhaoyancheng
 * @date 2021-05-20 22:58
 **/
public class BenzCar extends CarModel {
    @Override
    public void run() {
        System.out.println("benz run...");
    }

    @Override
    public void watch() {
        System.out.println("benz watch...");

    }
}
