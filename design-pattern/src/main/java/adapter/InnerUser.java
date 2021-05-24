package adapter;

/**
 * @author zhaoyancheng
 * @date 2021-05-20 07:49
 **/
public class InnerUser implements IinnerUser{
    @Override
    public void getPhone() {
        System.out.println("员工手机号");
    }

    @Override
    public void getName() {
        System.out.println("员工姓名");
    }
}
