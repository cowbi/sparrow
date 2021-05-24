package Observer;

import java.util.Observable;
import java.util.Observer;

/**
 * 观察者
 *
 * @author zhaoyancheng
 * @date 2021-05-23 23:07
 **/
public class ObserverWangsi implements Observer {

    @Override
    public void update(Observable o, Object arg) {

        System.out.println("wangsi 观察开始了");

        sendsihuang(arg.toString());

    }

    private void sendsihuang(String arg) {
        System.out.println("吴荒:" + arg);
    }
}
