package com.baixiao;

import com.baixiao.client.ComputeService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication

public class AgentApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(AgentApplication.class, args);
      ComputeService computeService = run.getBean(ComputeService.class);
       List<Integer> list =new ArrayList<>();
       list.add(1);
       list.add(2);
       list.add(3);
       list.add(4);
       list.add(5);
       list.add(6);
       list.add(7);
       list.add(8);
       list.add(9);
       List<Integer> result = computeService.computeBatch(list);
       System.out.println(result);
    }

}
