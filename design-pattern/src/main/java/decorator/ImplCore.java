package decorator;

/**
 * 未装饰的对象
 *
 * @author zhaoyancheng
 * @date 2021-05-23 15:18
 **/
public class ImplCore extends AbstractCore {
    @Override
    void report() {
        System.out.println("装饰对象子类");
    }

    @Override
    void sign() {
        System.out.println("签字子类");

    }
}
