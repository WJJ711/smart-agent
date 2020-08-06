package com.baixiao.agent.core.container;

import com.baixiao.agent.AgentCallback;

import com.baixiao.agent.AsyncHandler;
import com.baixiao.agent.AsyncResult;
import com.baixiao.agent.core.Agent;
import com.baixiao.agent.core.AgentMessage;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.text.MessageFormat;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/8 19:32
 */
@Slf4j
public abstract class AbstractAgent implements Agent {
    protected final Object realDefinitionInstance;

    public AbstractAgent(Object realDefinitionInstance) {
        this.realDefinitionInstance = realDefinitionInstance;
    }

    @Override
    public Object getAgentDefinitionInstance() {
        return this.realDefinitionInstance;
    }

    //真正干活的方法
    protected void handle(AgentMessage agentMessage) {
        Object realDefinitionInstance = agentMessage.getRealDefinitionInstance();
        Method agentMethod = agentMessage.getAgentMethod();
        Object[] agentMethodParameters = agentMessage.getAgentMethodParameters();

        try {
            //真正的干活处理方法,执行真实方法
            //这里才真正执行return AgentCallback.complete(in+1);所以返回的是AgentCallback类型
            Object result = agentMethod.invoke(realDefinitionInstance, agentMethodParameters);
            if (agentMessage.isAsync()) {
                //这个agentCallback在新开的线程中
                AgentCallback<?> agentCallback = (AgentCallback<?>) result;
                /**
                 * 这里只是定义了一个函数，真正执行的是
                 *   DefaultAgentContainer.getAsyncHandlerAgent().submit(()->asyncHandler.onCompleted(asyncResult));
                 *   这里的asyncResult 其实是agentCallback里的result
                 */
                agentCallback.onCompleted(asyncResult -> {
                    if (asyncResult.getException()!=null){
                        agentMessage.complete(asyncResult.getException());
                    }else {
                        //讲结果塞到message里面的callback中
                        agentMessage.complete(asyncResult.getResult());
                    }
                });
            }else {
                agentMessage.complete(result);
            }
        } catch (Exception e) {
           log.error(MessageFormat.format("agent method {} is invoked with exception.", name()), e);
            agentMessage.complete(e);
        }
    }
}
