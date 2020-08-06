package com.baixiao.container;

import com.baixiao.agent.AgentCallback;
import com.baixiao.agent.core.container.DefaultAgentContainer;
import com.baixiao.client.ComputeAgent;
import org.junit.jupiter.api.Test;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/10 22:44
 */
public class AgentContainerTest {

    @Test
    public void buildAgentDefinitionInstanceProxy(){
        DefaultAgentContainer agentContainer = new DefaultAgentContainer();
        ComputeAgent o = (ComputeAgent)agentContainer.buildAgentDefinitionInstanceProxy(new ComputeAgent());
        AgentCallback<Integer> integerAgentCallback = o.addOne(1);
        Integer integer = integerAgentCallback.get();
        System.out.println(integer);

    }
}
