package bridge;

/**
 * @author zhaoyancheng
 * @date 2021-05-22 23:17
 **/
public class HouseProduct extends AbstractProduct{
    @Override
    void produce() {
        System.out.println("生产住宅");
    }

    @Override
    void sell() {
        System.out.println("出售住宅");
    }
}
