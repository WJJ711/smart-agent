package com.baixiao.client;

import com.baixiao.agent.AgentCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/10 20:01
 */
@Service
public class ComputeService {

    @Autowired
    ComputeAgent computeAgent;
    public List<Integer> computeBatch(List<Integer> list){
        List<AgentCallback<Integer>> agentCallbacks=new ArrayList<>();
        list.forEach(integer -> agentCallbacks.add(computeAgent.addOne(integer)));
        List<Integer> result = AgentCallback.compose(agentCallbacks).get();
        return result;
    }
}
