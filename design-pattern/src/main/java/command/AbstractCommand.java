package command;

/**
 * @author zhaoyancheng
 * @date 2021-05-23 10:41
 **/
public abstract class AbstractCommand {

    protected  PageGroup pg = new PageGroup();
    protected  CodeGroup cg = new CodeGroup();
    protected  RequirementGroup rq = new RequirementGroup();

    public abstract void execute();

}
