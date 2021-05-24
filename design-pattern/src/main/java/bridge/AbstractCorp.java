package bridge;

/**
 * @author zhaoyancheng
 * @date 2021-05-22 23:13
 **/
public abstract class AbstractCorp {


    private AbstractProduct product;

    public AbstractCorp(AbstractProduct product) {
        this.product = product;
    }

    public void getMoney(){
        product.produce();
        product.sell();
    }


}
