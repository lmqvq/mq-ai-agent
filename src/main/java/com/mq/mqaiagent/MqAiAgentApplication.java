package com.mq.mqaiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE))
public class MqAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(MqAiAgentApplication.class, args);
    }

}
