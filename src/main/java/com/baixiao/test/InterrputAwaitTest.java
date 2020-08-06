package com.baixiao.test;

import java.util.concurrent.CountDownLatch;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/7 21:09
 */
public class InterrputAwaitTest {
   static CountDownLatch countDownLatch= new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                while (true&& !Thread.currentThread().isInterrupted()){
                    System.out.println("go");
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        //Thread.currentThread().interrupt();
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        thread.start();
        Thread.sleep(1000);
        thread.interrupt();
    }
}
