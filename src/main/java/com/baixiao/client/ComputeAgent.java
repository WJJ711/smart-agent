package com.baixiao.client;

import com.baixiao.agent.Agent;
import com.baixiao.agent.AgentCallback;
import com.baixiao.agent.Cached;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/10 16:02
 */
@Agent
@Cached(corePoolSize = 10,maximumPoolSize = 50)
public class ComputeAgent {

    public AgentCallback<Integer> addOne(Integer in){
        return AgentCallback.complete(in+1);
    }
}
