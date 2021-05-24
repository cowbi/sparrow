package strategy;

/**
 * 锦囊妙计
 * @author zhaoyancheng
 * @date 2021-05-19 13:27
 **/
public class SilkBag {

    private Istrategy istrategy;

    public SilkBag(Istrategy istrategy) {
        this.istrategy = istrategy;
    }

    public void run(){
        istrategy.operate();
    }
}
