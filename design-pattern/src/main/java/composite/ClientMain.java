package composite;

/**
 * 组合模式：composite
 * 解决整体与部分的结构。 树形接口尤其考虑
 *
 * 角色：
 * 1.整体与部分的公共抽象类
 * 2.branch
 * 3.leaf
 *
 * @author zhaoyancheng
 * @date 2021-05-23 16:38
 **/
public class ClientMain {

    public static void main(String[] args) {

        Branch ceo = new Branch("CEO",1,2);
        ceo.getParent();
        ceo.getInfo();
    }
}
