package core;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class StreamTest {

    @Test
    public void filter() {

        //得到Stream无意义,最终还是需要转换成我们认识的数据,比如通过count计算元素数量,
        //把流放到另外一个结合

        List<String> strings = Arrays.asList("abc", "def", "gkh", "abc");
        //返回符合条件的stream
        Stream<String> stringStream = strings.stream().filter(s -> "abc".equals(s));
        //计算流符合条件的流的数量
        long count = stringStream.count();

        //forEach遍历->打印元素
        strings.stream().forEach(System.out::println);

        //limit 获取到1个元素的stream
        Stream<String> limit = strings.stream().limit(1);
        //toArray 比如我们想看这个limitStream里面是什么，比如转换成String[],比如循环
        String[] array = limit.toArray(String[]::new);

        //map 对每个元素进行操作
        Stream<String> map = strings.stream().map(s -> s + "22");


    }

}
