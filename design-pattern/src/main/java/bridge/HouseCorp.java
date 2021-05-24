package bridge;

/**
 * @author zhaoyancheng
 * @date 2021-05-22 23:19
 **/
public class HouseCorp extends AbstractCorp{


    public HouseCorp(AbstractProduct product) {
        super(product);
    }

    @Override
    public void getMoney() {
        super.getMoney();
        System.out.println("房子赔钱了");
    }
}
