package com.baixiao.agent.core;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/8 17:43
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AgentMessageQueue {
    public static final int LOCK_TIMEOUT_SECONDS = 3;

    ReentrantLock queueLock = new ReentrantLock();
    Queue<AgentMessage> queue = new LinkedList<>();
    Agent owner;

    public AgentMessageQueue(Agent owner) {
        this.owner = owner;
    }
    public int size(){
        return doWithLock(in->queue.size(),null);
    }
    public void enqueue(AgentMessage agentMessage){
        doWithLock(message->queue.add(message),agentMessage);
    }
    public AgentMessage dequeue(){
        return doWithLock(in->queue.poll(),null);
    }
    private <T, R> R doWithLock(Function<T, R> action, T in) {
        if (queueLock.isHeldByCurrentThread()) {
            return action.apply(in);
        }
        try {
            if (!queueLock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new AgentException(AgentException.FAILED_TO_LOCK_AGENT_MESSAGE_QUEUE,
                        MessageFormat.format(AgentException.FAILED_TO_LOCK_AGENT_MESSAGE_QUEUE_MESSAGE, this.owner.name()));

            }
            return action.apply(in);
        } catch (InterruptedException e) {
            throw new AgentException(AgentException.FAILED_TO_LOCK_AGENT_MESSAGE_QUEUE,
                    MessageFormat.format(AgentException.FAILED_TO_LOCK_AGENT_MESSAGE_QUEUE_MESSAGE, this.owner.name()));
        } finally {
            queueLock.unlock();
        }
    }
}
