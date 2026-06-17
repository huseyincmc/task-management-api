package com.business.project.taskmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class TaskEventSubscriber implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(TaskEventSubscriber.class);

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("Received Redis Pub/Sub message from channel '{}': {}", channel, body);
    }
}
