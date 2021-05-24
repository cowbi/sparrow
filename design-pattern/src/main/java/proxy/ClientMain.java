package proxy;

/**
 * 代理模式 proxy
 *
 * 代理和被代理者有同样的接口
 * 1.代理
 * 2.被代理者
 * 3.使用者
 *
 * @author zhaoyancheng
 * @date 2021-05-19 14:02
 **/
public class ClientMain {

    public static void main(String[] args) {

        WomenKindProxy womenKindProxy = new WomenKindProxy();

        womenKindProxy.happy();

        womenKindProxy.lure();
    }
}
