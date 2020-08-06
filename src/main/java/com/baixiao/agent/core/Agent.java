package com.baixiao.agent.core;

import com.baixiao.agent.AgentState;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/8 17:19
 */
public interface Agent {
    long id();
    boolean isDisposed();
    String name();
    Object getAgentDefinitionInstance();
    AgentState state();
    void start();
    void receive(AgentMessage agentMessage);
    void exit();
}
