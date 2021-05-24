package Observer;

/**
 * 观察者 observer 发布/订阅模型（Publish/Subscribe）
 * java.util实现了
 *
 * 角色：
 * 1.被观察者 java实现了java.util.Observable
 * 2.观察者 java.util.Observer
 * @author zhaoyancheng
 * @date 2021-05-23 23:17
 **/
public class ClientMain {

    public static void main(String[] args) {

        Hanfei hanfei = new Hanfei();

        hanfei.addObserver(new ObserverLisi());
        hanfei.addObserver(new ObserverWangsi());

        hanfei.haveBreakfast();
        hanfei.haveFun();
    }
}
