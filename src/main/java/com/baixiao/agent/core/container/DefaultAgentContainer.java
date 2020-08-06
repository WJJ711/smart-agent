package com.baixiao.agent.core.container;
import com.baixiao.agent.*;
import com.baixiao.agent.core.Agent;
import com.baixiao.agent.core.AgentMessage;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.*;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/9 2:15
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class DefaultAgentContainer implements AgentContainer, BeanFactoryAware {
    ConcurrentHashMap<Object, Agent> agentsByProxy=new ConcurrentHashMap<>();
    ConcurrentHashMap<Long,Agent> agentsById=new ConcurrentHashMap<>();

    static final AsyncHandlerAgent asyncHandlerAgent;

    public static AsyncHandlerAgent getAsyncHandlerAgent() {
        return asyncHandlerAgent;
    }

    static {
        asyncHandlerAgent=new AsyncHandlerAgent();
    }

    //使用cglib生成代理类，在BeanPostProcessor的后置处理，如果用Agent注解标注，则在spring容器中注入代理类
    @Override
    public Object buildAgentDefinitionInstanceProxy(Object realDefinitionInstance) {
        Agent agent=createAgent(realDefinitionInstance);
        AgentMethodInvocationHandler agentMethodInvocationHandler = new AgentMethodInvocationHandler(agent, realDefinitionInstance);

        //CGlib动态代理
        Enhancer enhancer = new Enhancer();
        //被代理类
        enhancer.setSuperclass(realDefinitionInstance.getClass());
        enhancer.setClassLoader(realDefinitionInstance.getClass().getClassLoader());
       // enhancer.setNamingPolicy(new AgentNamingPolicy());
        //代理类
        enhancer.setCallback(agentMethodInvocationHandler);

        Object agentDefinitionInstanceProxy = enhancer.create();

        agentsByProxy.putIfAbsent(agentDefinitionInstanceProxy,agent);
        agentsById.putIfAbsent(agent.id(),agent);
        return agentDefinitionInstanceProxy;
    }

    /**
     * 传入实际的实例，生成一个agent，实例与agent绑定，agent里面有一个成员变量是线程池或者线程
     * @param realDefinitionInstance
     * @return
     */
    private Agent createAgent(Object realDefinitionInstance) {
        Agent agent;
        Class<?> clazz = realDefinitionInstance.getClass();
        if (clazz.isAnnotationPresent(Fixed.class)){
            Fixed fixed= clazz.getAnnotation(Fixed.class);
            ExecutorService executorService= Executors.newFixedThreadPool(fixed.nThreads());
            agent=new ExecutorServiceAgent(realDefinitionInstance,executorService);
        }else if (clazz.isAnnotationPresent(Cached.class)){
            Cached cached= clazz.getAnnotation(Cached.class);
            ThreadPoolExecutor threadPoolExecutor=new ThreadPoolExecutor(cached.corePoolSize(),cached.maximumPoolSize(),
                    cached.keepAliveTime(),cached.unit(),new LinkedBlockingQueue<>());
            agent=new ExecutorServiceAgent(realDefinitionInstance,threadPoolExecutor);
        }else if (clazz.isAnnotationPresent(Computation.class)){
            ExecutorService executorService=Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            agent=new ExecutorServiceAgent(realDefinitionInstance,executorService);
        }else {
            agent=new SingleThreadAgent(realDefinitionInstance);
        }
        agent.start();
        return agent;
    }

    @Override
    public Agent agentByProxy(Object agentDfinitionInstanceProxy) {
        return agentsByProxy.get(agentDfinitionInstanceProxy);
    }

    @Override
    public Agent agentById(long id) {
        return agentsById.get(id);
    }

    @Override
    public Agent agentByInstance(Object realDefinitionInstance) {
        for (Agent agent : agentsById.values()) {
            if (agent.getAgentDefinitionInstance().equals(realDefinitionInstance)){
                return agent;
            }
        }
        return null;
    }

    @Override
    public void exit() {
        agentsById.values().forEach(agent -> agent.exit());
        agentsById.clear();
        agentsByProxy.clear();

        asyncHandlerAgent.exit();
    }

    @Override
    public int size() {
        return agentsById.size();
    }

    /**
     * 代理类，里面有agent实例和实际bean对象
     */
    class AgentMethodInvocationHandler implements MethodInterceptor {
        private Agent agent;
        private Object realDefinitionInstance;

        public AgentMethodInvocationHandler(Agent agent, Object realDefinitionInstance) {
            this.agent = agent;
            this.realDefinitionInstance = realDefinitionInstance;
        }

      //  @Override
      //  public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
      //      if(exclude(method)){
      //          return method.invoke(o,objects);
      //      }
      //      AgentMessage agentMessage=createAgentMessage(method,objects);
      //      agentMessage.getDestination().receive(agentMessage);
//
      //      //如果是同步的
      //      if (!agentMessage.isAsync()){
      //          agentMessage.waitForSyncInvoking();
      //          return agentMessage.getResult();
      //      }
      //      return agentMessage.getAsyncAgentMethodCallback();
      //  }
//
        private AgentMessage createAgentMessage(Method method, Object[] objects) {
            Agent source=agentById(Thread.currentThread().getId());
            Agent destination=this.agent;
            if (AgentCallback.class.isAssignableFrom(method.getReturnType())){
                return AgentMessage.newASync(source,destination,realDefinitionInstance,method,objects);
            }
            return AgentMessage.newSync(source,destination,realDefinitionInstance,method,objects);
        }

        private boolean exclude(Method method) {
            String methodName = method.getName();
            String[] objectMethodNames=new String[]{"getClass","hashCode","equals","wait","notify","notifyAll","toString"};
            for (String objectMethodName : objectMethodNames) {
                if (methodName.equals(objectMethodName)){
                    return true;
                }
            }
            return false;
        }

        /**
         * 实际对象的方法被拦截后，执行该方法
         * 发送消息，同时，与实际对象绑定的agent类去接受消息，agent中的线程或者线程池去真正执行方法
         * @param o
         * @param method
         * @param objects
         * @param methodProxy
         * @return
         * @throws Throwable
         */
        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            /**
             * 如果是hashCode，toString等基础方法，则直接返回
             */
            if(exclude(method)){
                return methodProxy.invokeSuper(o,objects);
            }
            AgentMessage agentMessage=createAgentMessage(method,objects);
            agentMessage.getDestination().receive(agentMessage);

            //如果是同步的
            if (!agentMessage.isAsync()){
                agentMessage.waitForSyncInvoking();
                return agentMessage.getResult();
            }
            //返回message里的agentCallback
            return agentMessage.getAsyncAgentMethodCallback();
        }
    }
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof ConfigurableListableBeanFactory)){
            throw new IllegalArgumentException(
                    "ExtensionAutowiredBeanPostProcessor requires a ConfigurableListableBeanFactory: "+beanFactory);
        }
        ((ConfigurableListableBeanFactory) beanFactory).registerSingleton("asyncHandlerAgent",asyncHandlerAgent);
    }
   // static class AgentNamingPolicy extends DefaultNamingPolicy{
   //     @Override
   //     protected String getTag() {
   //         return "agent";
   //     }
   // }
}
