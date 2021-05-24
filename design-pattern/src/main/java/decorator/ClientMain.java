package decorator;

/**
 * 装饰者模式
 *
 * 1. 有一个特别核心的业务，或者最终的目标
 * 2. 在这个目标前加各种修饰，链式的进行
 * 示例场景： 让老爸签成绩单的字
 *
 * 角色：
 * 1. 核心业务抽象
 * 2. 核心业务实现
 * 3. 抽象装饰，继承核心业务抽象, 引用核心业务抽象
 * 4. 具体抽象
 * @author zhaoyancheng
 * @date 2021-05-23 15:10
 **/
public class ClientMain {

    public static void main(String[] args) {

        AbstractCore core = new ImplCore();
        //未装饰的核心业务
        //core.report();

        core = new ImplDecorator(core);

        //装饰第一次
        core.report();

        core = new ImplDecorator2(core);
        //装饰第二次
        core.report();

        core.sign();


    }
}
