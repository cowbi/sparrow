package core;

import org.openjdk.jol.info.ClassLayout;

/**
 * @author zhaoyancheng
 * @date 2021-04-03 16:50
 **/
public class HelloJol {

    private int i =0;
    private long j =0;

    public static void main(String[] args) {

        HelloJol object = new HelloJol();

        System.out.println(ClassLayout.parseInstance(object).toPrintable());
    }
}
