package com.demo.agent.common.rabbitmq;

import com.demo.agent.common.Constants;
import com.demo.agent.common.UserContext;
import com.demo.agent.model.base.RabbitMqTransportEntity;
import com.demo.agent.model.entity.SessionEntity;
import com.demo.agent.service.session.SessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import static com.demo.agent.common.Constants.QUEUE_NAME;


@Component
@Slf4j
public class MyMessageConsumer {

    @Resource
    private SessionService sessionService;

    // 指定程序监听的消息队列和确认机制
    @SneakyThrows
    @RabbitListener(queues = {QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String transprot, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        ObjectMapper objectMapper = new ObjectMapper();
        RabbitMqTransportEntity message = objectMapper.readValue(transprot, RabbitMqTransportEntity.class);
        log.info("receiveMessage message = {}", message);
        try {
            channel.basicAck(deliveryTag, false);
            SessionEntity session = sessionService.getById(message.getId());
            if(session == null){
                session = new SessionEntity();
                session.setId(message.getId());
                session.setUserId(UserContext.getUserId());
                session.setContent(message.getContent());
                session.setAgentId(1L);
                session.setUserId(1L);
                sessionService.save(session);
            }else{
                session.setContent(message.getContent());
                sessionService.updateById(session);
            }
        } catch (Exception e) {
            channel.basicNack(deliveryTag, false, false);

        }
    }

}
