package com.baixiao.agent;
import com.baixiao.agent.core.Agent;
/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/7 16:43
 */
public interface AgentContainer {
    Object buildAgentDefinitionInstanceProxy(Object realDefinitionInstance);
    Agent agentByProxy(Object agentDfinitionInstanceProxy);
    Agent agentById(long id);
    Agent agentByInstance(Object realDefinitionInstance);
    void exit();
    int size();
}
