package com.demo.agent.common.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.demo.agent.common.Constants.*;


@Slf4j
@Component
public class InitRabbitMqBean {

    @Value("${spring.rabbitmq.host}")
    private String host;  // 非static

    @PostConstruct
    public void init() {  // 实例方法，自动调用
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

            log.info("RabbitMQ 初始化成功");
        } catch (Exception e) {
            log.error("RabbitMQ 初始化失败", e);
        }
    }
}

