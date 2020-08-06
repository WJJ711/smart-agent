package com.baixiao.agent.core.processor;

import com.baixiao.agent.AgentContainer;
import com.baixiao.agent.core.Agent;
import com.baixiao.agent.core.container.AsyncHandlerAgent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.context.annotation.Configuration;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/9 11:07
 */
@Configuration
public class AgentBuildBeanPostProcessor implements DestructionAwareBeanPostProcessor {

    @Autowired
    private AgentContainer agentContainer;
    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {

        Agent agent = agentContainer.agentByInstance(bean);
        if (agent!=null){
            agent.exit();
        }
    }

    @Override
    public boolean requiresDestruction(Object bean) {

        return bean!=null&&(bean.getClass().isAssignableFrom(AsyncHandlerAgent.class)||
                bean.getClass().isAnnotationPresent(com.baixiao.agent.Agent.class));
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!bean.getClass().isAnnotationPresent(com.baixiao.agent.Agent.class)){
            return bean;
        }
        System.out.println("---------------生成反向代理---------------------");
        return agentContainer.buildAgentDefinitionInstanceProxy(bean);
    }
}
