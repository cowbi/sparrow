package factory.factoryMethod;

/**
 * 1. 任意定制交通工具
 *    implements movable
 * 2. 任意定制生产过程
 *    **factory
 * 3. 任意定制产品一族
 * @author zhaoyancheng
 * @date 2021-05-18 22:27
 **/
public class Main {

    public static void main(String[] args) {

        Movable movable = new CarFactory().create();

        movable.move();
    }
}
