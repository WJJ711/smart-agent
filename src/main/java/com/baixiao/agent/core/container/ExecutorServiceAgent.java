package com.baixiao.agent.core.container;

import com.baixiao.agent.AgentState;
import com.baixiao.agent.core.AgentException;
import com.baixiao.agent.core.AgentMessage;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/8 20:38
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExecutorServiceAgent extends AbstractAgent {

    AtomicReference<AgentState> state;
    final ExecutorService executorService;

    public ExecutorServiceAgent(Object realDefinitionInstance, ExecutorService executorService) {
        super(realDefinitionInstance);
        this.state = new AtomicReference<>(AgentState.NEW);
        this.executorService = executorService;
    }

    @Override
    public long id() {
        return hashCode();
    }

    @Override
    public boolean isDisposed() {
        return state.compareAndSet(AgentState.EXITED, AgentState.EXITED);
    }

    @Override
    public String name() {
        return MessageFormat.format("ThreadPoolAgent_{1}", id());
    }

    @Override
    public AgentState state() {
        return state.get();
    }

    @Override
    public void start() {
        assertNotNew();
        state.compareAndSet(AgentState.NEW, AgentState.RUNNING);
    }


    @Override
    public void receive(AgentMessage agentMessage) {
        assertNotStarted();
        executorService.submit(() -> handle(agentMessage));

    }


    @Override
    public void exit() {
        if (state.get().equals(AgentState.RUNNING)) {
            state.set(AgentState.EXITED);
            executorService.shutdown();
        }
    }

    private void assertNotNew() {
        if (!state.get().equals(AgentState.NEW)) {
            throw new AgentException(AgentException.AGENT_NOT_NEW, MessageFormat.format(AgentException.AGENT_NOT_NEW_MESSAGE, name()));
        }
    }

    private void assertNotStarted() {
        if (state.get().equals(AgentState.NEW) || state.get().equals(AgentState.EXITED)) {
            throw new AgentException(AgentException.AGENT_NOT_STARTED, MessageFormat.format(AgentException.AGENT_NOT_STARTED_MESSAGE, name()));
        }
    }
}
