package com.baixiao.agent.core.container;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/8 20:31
 */
@Slf4j
public class AsyncHandlerAgent{
    private ExecutorService executorService=new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),20,60, TimeUnit.SECONDS,new LinkedBlockingQueue<>());
    public void submit(Runnable runnable){
        //这里直接提交runnable不就好了么
       //executorService.submit(()->{
       //    try {
       //        runnable.run();
       //    }catch (Exception ex){
       //      log.warn("Invoke the callback failed",ex);
       //    }
       //});
        executorService.submit(runnable);
    }
    public void exit(){executorService.shutdown();}
}
