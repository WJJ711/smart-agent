package com.baixiao.agent;

import com.baixiao.agent.core.AgentException;
import com.baixiao.agent.core.container.DefaultAgentContainer;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/7 19:25
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AgentCallback<T> {
    AsyncHandler<T> asyncHandler;
    volatile boolean isDone;
    T result;
    Exception exception;
    final CountDownLatch countDownLatch=new CountDownLatch(1);

    public boolean isDone(){return this.isDone;}

    public static <T> AgentCallback<T> complete(T result){
        AgentCallback<T> callback=new AgentCallback<>();
        callback.completeWithResult(result);
        return callback;
    }
    public static <T> AgentCallback exception(Exception exception){
        AgentCallback<T> callback = new AgentCallback<>();
        callback.completeWithException(exception);
        return callback;
    }
    public static <T> AgentCallback<List<T>> compose(List<AgentCallback<T>> agentCallbacks){
        Assert.isTrue(null!=agentCallbacks&& !agentCallbacks.isEmpty(),"agentCallbacks can not be empty.");

        AgentCallback<List<T>> composedAgentCallback=new AgentCallback<>();
        List<T> result=new ArrayList<>();

        AtomicInteger counter=new AtomicInteger(agentCallbacks.size());
        for (AgentCallback<T> agentCallback : agentCallbacks) {
            agentCallback.onCompleted(new AsyncHandler<T>() {
                @Override
                public void onCompleted(AsyncResult<T> asyncResult) {
                    if(asyncResult.getResult()!=null){
                        synchronized (result){
                            result.add(asyncResult.getResult());
                        }
                    }
                    if (counter.decrementAndGet()==0){
                        composedAgentCallback.completeWithResult(result);
                    }
                }
            });
        }
        return composedAgentCallback;
    }
    public <R> AgentCallback<R> link(Function<T,AgentCallback<R>> transformer){
        Assert.notNull(transformer,"next can not be null");
        AgentCallback<R> agentCallback = new AgentCallback<>();
        this.onCompleted(new AsyncHandler<T>() {
            @Override
            public void onCompleted(AsyncResult<T> asyncResult) {
                transformer.apply(asyncResult.getResult()).onCompleted(new AsyncHandler<R>() {
                    @Override
                    public void onCompleted(AsyncResult<R> linkedAsyncResult) {
                        if (linkedAsyncResult.getException()!=null){
                            agentCallback.completeWithException(linkedAsyncResult.getException());
                        }else {
                            agentCallback.completeWithResult(linkedAsyncResult.getResult());
                        }
                    }
                });
            }
        });
        return agentCallback;
    }
    public <R> AgentCallback<R> link(AgentCallback<R> agentCallback){
        Assert.notNull(agentCallback,"next can not be null");
        this.onCompleted(new AsyncHandler<T>() {
            @Override
            public void onCompleted(AsyncResult<T> asyncResult) {
                agentCallback.onCompleted(new AsyncHandler<R>() {
                    @Override
                    public void onCompleted(AsyncResult<R> linkedAsyncResult) {
                        if (linkedAsyncResult.getException()!=null){
                            agentCallback.completeWithException(linkedAsyncResult.getException());
                        }else {
                            agentCallback.completeWithResult(linkedAsyncResult.getResult());
                        }
                    }
                });
            }
        });
        return agentCallback;
    }
    /**
     * 阻塞获取结果
     * @return
     */
    public T get(){
        if (!isDone){
            while (!isDone&&!Thread.currentThread().isInterrupted()){
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    exception=e;
                }
            }
        }
        if (exception!=null){
            throw new AgentException(AgentException.ASYNC_WITH_EXCEPTION,AgentException.ASYNC_WITH_EXCEPTION_MESSAGE,exception);

        }
        return result;
    }
    public void onCompleted(AsyncHandler<T> asyncHandler){
        this.asyncHandler=asyncHandler;
        callAsyncHandler();
    }

    public void completeWithResult(Object result){
        this.result=(T)result;
        isDone=true;
        countDownLatch.countDown();
        callAsyncHandler();
    }
    public void completeWithException(Exception exception){
        this.exception=exception;
        this.isDone=true;
        countDownLatch.countDown();
        callAsyncHandler();
    }
    /**
     * 调用回调方法
     */
    private void callAsyncHandler(){
        if (null!=this.asyncHandler&&this.isDone){
            AsyncResult<T> asyncResult = new AsyncResult<>();
            asyncResult.setException(exception);
            asyncResult.setResult(result);

            DefaultAgentContainer.getAsyncHandlerAgent().submit(()->asyncHandler.onCompleted(asyncResult));
        }
    }
}
