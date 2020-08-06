package com.baixiao.agent.core.container;

import com.baixiao.agent.AgentState;
import com.baixiao.agent.core.AgentException;
import com.baixiao.agent.core.AgentMessage;
import com.baixiao.agent.core.AgentMessageQueue;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/8 21:44
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SingleThreadAgent extends AbstractAgent {
    AtomicReference<AgentState> state;

    final Object waitForAgentMessageSignal;
    final AgentMessageQueue agentMessageQueue;
    final Thread processor;
    public SingleThreadAgent(Object realDefinitionInstance) {
        super(realDefinitionInstance);

        state=new AtomicReference<>(AgentState.NEW);
        waitForAgentMessageSignal=new Object();
        agentMessageQueue=new AgentMessageQueue(this);
        processor=new Thread(this::process);
        //相当于
        //processor=new Thread(()->process());
        processor.setName("SingleThreadAgent_"+realDefinitionInstance.getClass().getName()+"_"+processor.getId());
    }

    @Override
    public long id() {
        return processor.getId();
    }

    @Override
    public boolean isDisposed() {
        return state.compareAndSet(AgentState.EXITED,AgentState.EXITED);
    }

    @Override
    public String name() {
        return processor.getName();
    }

    @Override
    public AgentState state() {
        return this.state.get();
    }


    @Override
    public void start() {
        if (state.compareAndSet(AgentState.NEW,AgentState.RUNNING)){
            processor.start();
          log.debug("agent {} is started.",name());
        }
    }

    /**
     * 接收到消息后，去唤醒agent中的线程
     * @param agentMessage
     */
    @Override
    public void receive(AgentMessage agentMessage) {
        assertNotStarted();
        agentMessageQueue.enqueue(agentMessage);
        notifyForAgentMessage();
    }

    private void notifyForAgentMessage() {
        synchronized (waitForAgentMessageSignal){
            waitForAgentMessageSignal.notifyAll();
            log.debug("agent {} is notified",name());
        }
    }

    /**
     * 在创建agent的时候就启动线程，如果agentMessageQueue中没有message，则wait
     */
    private void process(){
        while (!state.get().equals(AgentState.EXITED)){
            AgentMessage agentMessage = agentMessageQueue.dequeue();
            if (agentMessage==null){
                if (!state.get().equals(AgentState.EXITED)){
                    state.set(AgentState.BLOCKED);
                    waitForAgentMessage();
                }
                if (!state.get().equals(AgentState.EXITED)){
                    state.set(AgentState.RUNNING);
                }
                continue;
            }
            /**
             * 处理计算
             */
            handle(agentMessage);
        }
        if (state.get().equals(AgentState.EXITED)){
            for (AgentMessage agentMessage=agentMessageQueue.dequeue();agentMessage!=null;agentMessage=agentMessageQueue.dequeue()){
                handle(agentMessage);
            }
        }
    }
    private void waitForAgentMessage(){
        synchronized (waitForAgentMessageSignal){
            try {
                //必须判断消息队列是否为空，避免在消息队列为空下，进行判断的过程中，有新的消息到达
                if (agentMessageQueue.size()>0){
                    return;
                }
               log.debug("agent {} is blocking.");
                waitForAgentMessageSignal.wait();
            } catch (InterruptedException e) {
               log.warn("agent {} failed to wait for message.",name());
            }
        }
    }
    @Override
    public void exit() {
        state.set(AgentState.EXITED);
        notifyForAgentMessage();
       log.debug("agent {} is exiting",name());

        try {
            processor.join(3000);
            log.debug("agent {} is exited.",name());
        } catch (InterruptedException e) {
            log.debug("agent {} is exited with exception.",name());
            processor.interrupt();
        }
    }
    private void assertNotStarted() {
        if (state.get().equals(AgentState.NEW)||state.get().equals(AgentState.EXITED)){
            throw new AgentException(AgentException.AGENT_NOT_STARTED, MessageFormat.format(AgentException.AGENT_NOT_STARTED_MESSAGE,name()));
        }
    }

}
