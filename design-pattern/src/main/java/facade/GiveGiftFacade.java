package facade;

/**
 * 新需求：买花要包装
 * @author zhaoyancheng
 * @date 2021-05-19 22:42
 **/
public class GiveGiftFacade {


    private GiveGift giveGift = new GiveGiftImpl();

    private GiveGiftFacadeExt giveGiftFacadeExt = new GiveGiftFacadeExt();

    public void buyAndGive(String giftName,String name){

        giveGift.buyGift(giftName);

        giveGiftFacadeExt.check();

        giveGift.give(name);
    }
}
