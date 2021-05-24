package facade;

/**
 * @author zhaoyancheng
 * @date 2021-05-19 22:40
 **/
public class GiveGiftImpl implements GiveGift {

    @Override
    public void buyGift(String giftname) {
        System.out.println("买" + giftname);
    }

    @Override
    public void give(String name) {
        System.out.println("送给" + name);

    }
}
