package facade;

/**
 * 门面模式 封装实现细节，调用者只提供无聊到，具体实现过程不管。只要结果，老板只要结果，不要过程。
 *
 * 模拟场景：买花仙佛 - 政府部门 - 接待
 * @author zhaoyancheng
 * @date 2021-05-19 22:36
 **/
public class ClientMain {

    public static void main(String[] args) {

        GiveGiftFacade giveGiftFacade = new GiveGiftFacade();

        giveGiftFacade.buyAndGive("玫瑰","小红");

    }
}
