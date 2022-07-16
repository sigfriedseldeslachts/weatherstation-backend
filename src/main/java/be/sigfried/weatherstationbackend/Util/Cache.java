package be.sigfried.weatherstationbackend.Util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Callable;

public class Cache {

    public RedisCommands<String, String> redisCommands;
    
    public final ObjectMapper objectMapper;

    public Cache(RedisCommands<String, String> redisCommands) {
        this.redisCommands = redisCommands;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public String get(String cacheKey) {
        return redisCommands.get(cacheKey);
    }

    public String remember(String cacheKey, Callable<Object> action, Duration duration) throws Exception {
        // Check if the key exists in the cache
        if (get(cacheKey) != null) {
            return get(cacheKey);
        } else {
            String value = objectMapper.writeValueAsString(action.call());
            redisCommands.setex(cacheKey, duration.toSeconds(), value);
            return value;
        }
    }

    public <T> T remember(String cacheKey, Callable<Object> action, Duration duration, Class<T> valueType) throws Exception {
        try {
            return objectMapper.readValue(remember(cacheKey, action, duration), valueType);
        } catch (Exception e) {
            LoggerFactory.getLogger(Cache.class).error("Error while reading from cache", e);
            return null;
        }
    }

    public <T> T remember(String cacheKey, Callable<Object> action, Duration duration, TypeReference<T> valueType) {
        try {
            return objectMapper.readValue(remember(cacheKey, action, duration), valueType);
        } catch (Exception e) {
            LoggerFactory.getLogger(Cache.class).error("Error while reading from cache", e);
            return null;
        }
    }
}
