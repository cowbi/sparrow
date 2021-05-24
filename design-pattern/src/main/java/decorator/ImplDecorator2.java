package decorator;

/**
 * @author zhaoyancheng
 * @date 2021-05-23 15:20
 **/
public class ImplDecorator2 extends AbstractDecorator{

    public ImplDecorator2(AbstractCore core) {
        super(core);
    }

    private void myreport(){
        System.out.println("myreport111111!!");
    }

    @Override
    void report() {
        this.myreport();
        super.report();
    }
}
