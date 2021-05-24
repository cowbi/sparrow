package command;

/**
 * @author zhaoyancheng
 * @date 2021-05-23 10:47
 **/
public class Invoker {

    private AbstractCommand command;

    public Invoker(AbstractCommand command) {
        this.command = command;
    }

    public void action() {
        command.execute();
    }
}
