package core.juc;

import java.util.concurrent.atomic.AtomicInteger;

public class CasTest {

    private volatile static int a;

    private static void a() {
        a++;
    }

    public static void main(String[] args) {


        for (int i = 0; i <= 10000; i++) {
            new Thread(() -> {
                a();
                System.out.println(a);
            }).start();

            new Thread(() -> {
                a();
                System.out.println(a);
            }).start();
        }


    }
}



