package adapter;

/**
 * @author zhaoyancheng
 * @date 2021-05-20 07:53
 **/
public class UserAdapter extends OutUserInfo implements IinnerUser{


    @Override
    public void getPhone() {
        this.getUserInfo().get("phone");
    }

    @Override
    public void getName() {
        this.getUserInfo().get("name");

    }
}
