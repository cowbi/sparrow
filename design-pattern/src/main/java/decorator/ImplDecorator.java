package decorator;

/**
 * @author zhaoyancheng
 * @date 2021-05-23 15:20
 **/
public class ImplDecorator extends AbstractDecorator{

    public ImplDecorator(AbstractCore core) {
        super(core);
    }

    private void myreport(){
        System.out.println("myreport!!");
    }

    @Override
    void report() {
        this.myreport();
        super.report();
    }
}
