package com.baixiao;

import com.baixiao.agent.AgentCallback;
import com.baixiao.client.ComputeAgent;
import com.baixiao.client.ComputeService;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Collections;

@SpringBootTest
@ComponentScan("com.baixiao")
class AgentApplicationTests {

    @Autowired
    ComputeService computeService;

    @Autowired
    ComputeAgent computeAgent;
    @Test
    void contextLoads() {
        val re=computeService.computeBatch(Collections.singletonList(1));
        System.out.println(re);
    }

    @Test
    void addOneTest(){
        AgentCallback<Integer> integerAgentCallback = computeAgent.addOne(1);
        System.out.println(integerAgentCallback.get());
    }

}
