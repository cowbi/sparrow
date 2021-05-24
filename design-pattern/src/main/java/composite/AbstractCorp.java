package composite;

/**
 * @author zhaoyancheng
 * @date 2021-05-23 16:55
 **/
public abstract class AbstractCorp {

    private String name;
    private int age;
    private int sex;

    private AbstractCorp parent;

    public AbstractCorp(String name, int age, int sex) {
        this.name = name;
        this.age = age;
        this.sex = sex;
    }

    public void getInfo() {
        System.out.println("name:" + name + ",age:" + age + ",sex:" + sex);
    }

    public void setParent(AbstractCorp corp) {
        this.parent = corp;
    }

    public AbstractCorp getParent(){
        return this.parent;
    }

}
