package adapter;

import java.io.*;

/**
 * 适配器就  变压器 插线板
 * 把各种不同类型接口统一
 *
 * 针对类
 *
 * 适配器模式不适合在系统设计阶段采用，没有一个系统分析师会在做详设的时候考虑使用适配器模式，
 * 这个模式使用的主要场景是扩展应用中，就像我们上面的那个例子一样，系统扩展了，不符合原有设计的
 * 时候才考虑通过适配器模式减少代码修改带来的风险。
 * @author zhaoyancheng
 * @date 2021-05-20 07:41
 **/
public class ClientMain {

    public static void main(String[] args) throws IOException {

        //适配器
        FileInputStream fis = new FileInputStream("c:/test.text");
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);
        String line = br.readLine();
        while (line != null && !line.equals("")) {
            System.out.println(line);
        }
        br.close();
    }
}
