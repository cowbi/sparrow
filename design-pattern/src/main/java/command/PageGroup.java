package command;

/**
 * 美工组
 * @author zhaoyancheng
 * @date 2021-05-23 10:38
 **/
public class PageGroup extends AbstractGroup{
    
    
    @Override
    void find() {
        System.out.println("代码组 查询需求");
    }

    @Override
    void add() {
        System.out.println("美工组 新增需求");

    }

    @Override
    void update() {
        System.out.println("美工组 修改需求");

    }

    @Override
    void delete() {
        System.out.println("美工组 删除需求");
    }
}
