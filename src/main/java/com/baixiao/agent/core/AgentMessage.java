package com.baixiao.agent.core;

import com.baixiao.agent.AgentCallback;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.concurrent.CountDownLatch;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/8 15:59
 */
@Getter
@FieldDefaults(level=AccessLevel.PRIVATE)
public class AgentMessage {
    long createdTimeMillis=System.currentTimeMillis();
    long completedTimeMillis=-1;

    //这里错了，source应该是真实的类
    Agent source;
    Agent destination;

    Object realDefinitionInstance;
    Method agentMethod;
    Object[] agentMethodParameters;

    boolean isAsync;
    AgentCallback<?> asyncAgentMethodCallback;

    Object result;
    Exception exception;
    final CountDownLatch syncInvokingCountDown=new CountDownLatch(1);

    public static AgentMessage newSync(Agent source, Agent destination, Object realDefinitionInstance, Method agentMethod, Object[] agentMethodParameters) {
        AgentMessage agentMessage = new AgentMessage();
        agentMessage.isAsync=false;
        init(agentMessage,source,destination,realDefinitionInstance,agentMethod,agentMethodParameters);
        return agentMessage;
    }
    public static AgentMessage newASync(Agent source, Agent destination, Object realDefinitionInstance, Method agentMethod, Object[] agentMethodParameters) {
        AgentMessage agentMessage = new AgentMessage();
        agentMessage.isAsync=true;
        try {
            agentMessage.asyncAgentMethodCallback=(AgentCallback<?>)agentMethod.getReturnType().newInstance();
        } catch (Exception e) {
            //Ignore
        }
        if (!AgentCallback.class.isAssignableFrom(agentMethod.getReturnType())){
            throw new AgentException(AgentException.ASYNC_METHOD_RESULT_TYPE_ERROR,
                    MessageFormat.format(AgentException.ASYNC_METHOD_RESULT_TYPE_ERROR_MESSAGE,realDefinitionInstance.getClass().getName()));

        }
        init(agentMessage,source,destination,realDefinitionInstance,agentMethod,agentMethodParameters);
        return agentMessage;
    }

    private static void init(AgentMessage agentMessage, Agent source, Agent destination, Object realDefinitionInstance, Method agentMethod, Object[] agentMethodParameters) {
        agentMessage.source = source;
        agentMessage.destination = destination;
        agentMessage.realDefinitionInstance = realDefinitionInstance;
        agentMessage.agentMethod = agentMethod;
        agentMessage.agentMethodParameters = agentMethodParameters;
    }

    public void complete(Object result){
        this.result=result;
        this.completedTimeMillis=System.currentTimeMillis();
        if (isAsync){
            this.asyncAgentMethodCallback.completeWithResult(result);
        }else {
            this.notifySyncInvoking();
        }
    }
    public void complete(Exception exception){
        this.result=null;
        this.completedTimeMillis=System.currentTimeMillis();
        this.exception=exception;
        if (isAsync){
            this.asyncAgentMethodCallback.completeWithException(exception);
        }else {
            this.notifySyncInvoking();
        }
    }
    public void waitForSyncInvoking(){
        if (isAsync){
            return;
        }
        try {
            syncInvokingCountDown.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AgentException(AgentException.FAILED_TO_WAIT_FOR_SYNC_INVOKING,
                    MessageFormat.format(AgentException.FAILED_TO_WAIT_FOR_SYNC_INVOKING_MESSAGE,this.destination==null?"":this.destination.name()));
        }
    }

    private void notifySyncInvoking() {
        syncInvokingCountDown.countDown();
    }
}
