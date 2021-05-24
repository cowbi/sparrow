package proxy;

/**
 * @author zhaoyancheng
 * @date 2021-05-19 14:29
 **/
public class WomenKindProxy implements WomenKind{

    private WomenKind womenKind;

    public WomenKindProxy() {
        this.womenKind = new Jinlian();
    }

    public WomenKindProxy(WomenKind womenKind) {
        this.womenKind = womenKind;
    }

    @Override
    public void happy() {
        //do something
        womenKind.happy();
    }

    @Override
    public void lure() {
        //do something
        womenKind.lure();
    }
}
