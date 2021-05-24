package extendstest;

/**
 * @author zhaoyancheng
 * @date 2021-05-21 14:50
 **/
public class Child extends Father{
    @Override
    void test() {
        System.out.println("子类test");
    }

    public static void main(String[] args) {
        Child child = new Child();
        child.test();
    }
}
