package builder;

/**
 * @author zhaoyancheng
 * @date 2021-05-20 22:57
 **/
public class BmwCar extends CarModel{
    @Override
    public void run() {
        System.out.println("bmw run");
    }

    @Override
    public void watch() {
        System.out.println("bmw look");
    }
}
