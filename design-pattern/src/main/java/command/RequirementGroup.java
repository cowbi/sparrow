package command;

/**
 * 需求组
 * @author zhaoyancheng
 * @date 2021-05-23 10:38
 **/
public class RequirementGroup extends AbstractGroup{
    @Override
    void find() {
        System.out.println("需求组 查询需求");
    }

    @Override
    void add() {
        System.out.println("需求组 新增需求");

    }

    @Override
    void update() {
        System.out.println("需求组 修改需求");

    }

    @Override
    void delete() {
        System.out.println("需求组 删除需求");
    }
}
