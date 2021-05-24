package command;

/**
 * 命令模式：command
 * <p>
 * 要素：1.抽象命令
 * 2.具体命令-封装命令
 * 3.执行命令的抽象类
 * 4.执行命令具体类
 * 5.命令的调用者
 *
 * @author zhaoyancheng
 * @date 2021-05-23 10:49
 **/
public class ClientMain {

    public static void main(String[] args) {

        AbstractCommand command = new AddCommand();
        Invoker invoker = new Invoker(command);
        invoker.action();
    }
}
