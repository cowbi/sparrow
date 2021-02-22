package core;

public class ThreadStateTest {

     static class MyThread extends Thread{

        @Override
        public void run() {

            System.out.println("run方法::"+ this.getState());
        }
    }

    public static void main(String[] args) throws InterruptedException {

        MyThread t1 = new MyThread();

        System.out.println("执行new后状态：："+t1.getState());

        t1.start();

        System.out.println("执行start后状态：："+t1.getState());

        t1.sleep(5);

        System.out.println("执行wait后的状态：："+t1.getState());


    }

}
