package com.demo.agent.tool;

import com.demo.agent.common.UserContext;
import com.demo.agent.common.rabbitmq.MyMessageProducer;
import com.demo.agent.mapper.SessionMapper;
import com.demo.agent.model.base.RabbitMqTransportEntity;
import com.demo.agent.model.entity.SessionEntity;
import com.demo.agent.service.session.SessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static com.demo.agent.common.Constants.*;
import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;

@Component
public class PersistentChatMemoryStore implements ChatMemoryStore {

    @Resource
    private SessionService sessionService;


    @Resource
    private MyMessageProducer myMessageProducer;

    @Autowired
    private StringRedisTemplate redisTemplate;



    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = MESSAGE_MEMORY_PREFIX + memoryId;
        String content = redisTemplate.opsForValue().get(key);
        if(content == null){
            return messagesFromJson("[]");
        }
        List<ChatMessage> chatMessages = messagesFromJson(content);
        return chatMessages;
    }

    @Override
    public void updateMessages(Object memoryId,List<ChatMessage> messages) {
        String content = messagesToJson(messages);
        String key = MESSAGE_MEMORY_PREFIX + memoryId;
        redisTemplate.opsForValue().set(key,content);
        RabbitMqTransportEntity message = new RabbitMqTransportEntity();
        message.setId((Long) memoryId);
        message.setContent(content);
        message.setDateTime(new Date());
        ObjectMapper objectMapper = new ObjectMapper();
        String messageJson = "";
        try {
            messageJson = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        myMessageProducer.sendMessage(EXCHANGE_NAME,ROUTING_KEY,messageJson);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = MESSAGE_MEMORY_PREFIX + memoryId;
        redisTemplate.delete(key);
    }
}
