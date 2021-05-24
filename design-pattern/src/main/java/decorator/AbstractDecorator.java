package decorator;

/**
 * 装饰者 装饰core
 *
 * @author zhaoyancheng
 * @date 2021-05-23 15:16
 **/
public class AbstractDecorator extends AbstractCore {

    private AbstractCore core;

    public AbstractDecorator(AbstractCore core) {
        this.core = core;
    }


    @Override
    void report() {
        this.core.report();
    }

    @Override
    void sign() {
        this.core.sign();
    }
}
