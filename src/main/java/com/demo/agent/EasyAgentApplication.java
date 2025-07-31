package com.demo.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EasyAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyAgentApplication.class, args);
    }

}
