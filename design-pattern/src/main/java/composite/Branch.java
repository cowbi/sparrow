package composite;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaoyancheng
 * @date 2021-05-23 17:01
 **/
public class Branch extends AbstractCorp {

    public Branch(String name, int age, int sex) {
        super(name, age, sex);
    }

    List<AbstractCorp> list = new ArrayList<>();

    public void add(AbstractCorp corp){
        corp.setParent(this);
        list.add(corp);
    }

    public List<AbstractCorp> getSubordinate(){
        return this.list;
    }
}
