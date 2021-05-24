package Observer;

import java.util.Observable;
import java.util.Observer;

/**
 * 观察者
 *
 * @author zhaoyancheng
 * @date 2021-05-23 23:07
 **/
public class ObserverLisi implements Observer {

    @Override
    public void update(Observable o, Object arg) {

        System.out.println("lisi 观察开始了");

        sendsihuang(arg.toString());

    }

    private void sendsihuang(String arg) {
        System.out.println("四荒:" + arg);
    }
}
