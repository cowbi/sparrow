package bridge;

/**
 * @author zhaoyancheng
 * @date 2021-05-22 23:19
 **/
public class ShanzaiCorp extends AbstractCorp{


    public ShanzaiCorp(AbstractProduct product) {
        super(product);
    }

    @Override
    public void getMoney() {
        super.getMoney();
        System.out.println("山寨公司赚钱了");
    }
}
