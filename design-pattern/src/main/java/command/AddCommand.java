package command;

/**
 * @author zhaoyancheng
 * @date 2021-05-23 10:44
 **/
public class AddCommand extends AbstractCommand {

    @Override
    public void execute() {

        super.rq.find();
        super.pg.add();
        super.cg.add();

    }
}
