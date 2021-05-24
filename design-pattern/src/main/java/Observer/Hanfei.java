package Observer;

import java.util.Observable;

/**
 * @author zhaoyancheng
 * @date 2021-05-23 23:12
 **/
public class Hanfei extends Observable {


    public void  haveBreakfast(){
        System.out.println("韩非子 开始吃饭了");
        super.setChanged();
        super.notifyObservers("吃饭");
    }

    public void haveFun(){
        System.out.println("韩非子 开始fun了");
        super.setChanged();
        super.notifyObservers("fun");
    }
}
