package com.ming.mymcpclient.memory.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.ming.mymcpclient.memory.redis.serializer.MessageDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.util.Assert;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

public class RedisChatMemoryRepository implements ChatMemoryRepository, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(RedisChatMemoryRepository.class);

    private static final String DEFAULT_KEY_PREFIX = "spring_ai_chat_memory:";

    private final JedisPool jedisPool;

    private final ObjectMapper objectMapper;

    private RedisChatMemoryRepository(JedisPool jedisPool) {
        Assert.notNull(jedisPool, "jedisPool cannot be null");
        this.jedisPool = jedisPool;
        this.objectMapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(Message.class, new MessageDeserializer());
        this.objectMapper.registerModule(module);
    }

    public static RedisBuilder builder() {
        return new RedisBuilder();
    }

    @Override
    public List<String> findConversationIds() {
        try (Jedis jedis = jedisPool.getResource()) {
            final List<String> keys = new ArrayList<>(jedis.keys(DEFAULT_KEY_PREFIX + "*"));
            return keys.stream()
                    .map(key -> key.substring(DEFAULT_KEY_PREFIX.length()))
                    .toList();
        }
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        try (Jedis jedis = jedisPool.getResource()) {
            final String key = DEFAULT_KEY_PREFIX + conversationId;
            final List<String> messageStrings = jedis.lrange(key, 0, -1);
            final List<Message> messages = new ArrayList<>();

            for (String messageString : messageStrings) {
                try {
                    final Message message = objectMapper.readValue(messageString, Message.class);
                    messages.add(message);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error deserializing message", e);
                }
            }
            return messages;
        }
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.notNull(messages, "messages cannot be null");
        Assert.noNullElements(messages, "messages cannot contain null elements");

        try (Jedis jedis = jedisPool.getResource()) {
            final String key = DEFAULT_KEY_PREFIX + conversationId;
            // Clear existing messages first
            deleteByConversationId(conversationId);

            // Add all messages in order
            for (Message message : messages) {
                try {
                    final String messageJson = objectMapper.writeValueAsString(message);
                    jedis.rpush(key, messageJson);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error serializing message", e);
                }
            }
        }
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        try (Jedis jedis = jedisPool.getResource()) {
            final String key = DEFAULT_KEY_PREFIX + conversationId;
            jedis.del(key);
        }
    }

    /**
     * Clear messages over the limit for a conversation
     *
     * @param conversationId the conversation ID
     * @param maxLimit       maximum number of messages to keep
     * @param deleteSize     number of messages to delete when over limit
     */
    public void clearOverLimit(String conversationId, int maxLimit, int deleteSize) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        try (Jedis jedis = jedisPool.getResource()) {
            final String key = DEFAULT_KEY_PREFIX + conversationId;
            List<String> all = jedis.lrange(key, 0, -1);

            if (all.size() >= maxLimit) {
                all = all.stream().skip(Math.max(0, deleteSize)).toList();
                deleteByConversationId(conversationId);
                for (String message : all) {
                    jedis.rpush(key, message);
                }
            }
        }
    }

    @Override
    public void close() {
        if (jedisPool != null) {
            jedisPool.close();
            logger.info("Redis connection pool closed");
        }
    }

    public static class RedisBuilder {

        private String host = "127.0.0.1";

        private int port = 6379;

        private String password;

        private int timeout = 2000;

        private JedisPoolConfig poolConfig;

        public RedisBuilder host(String host) {
            this.host = host;
            return this;
        }

        public RedisBuilder port(int port) {
            this.port = port;
            return this;
        }

        public RedisBuilder password(String password) {
            this.password = password;
            return this;
        }

        public RedisBuilder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public RedisBuilder poolConfig(JedisPoolConfig poolConfig) {
            this.poolConfig = poolConfig;
            return this;
        }

        public RedisChatMemoryRepository build() {
            if (poolConfig == null) {
                poolConfig = new JedisPoolConfig();
            }
            final JedisPool jedisPool = new JedisPool(poolConfig, host, port, timeout, password);
            return new RedisChatMemoryRepository(jedisPool);
        }

    }

}
