package factory.factoryMethod;

/**
 * @author zhaoyancheng
 * @date 2021-05-18 22:28
 **/
public class Car implements Movable{
    @Override
    public void move() {

        System.out.println("car move ------");
    }
}
