package be.sigfried.weatherstationbackend;

import be.sigfried.weatherstationbackend.Util.Cache;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

@Configuration
public class RedisConfig {

    @Value("${REDIS_CONNECTION_URL}")
    public String REDIS_CONNECTION_URL;

    @Bean
    public RedisClient redisClient() {
        return RedisClient.create(REDIS_CONNECTION_URL);
    }

    @Bean
    public StatefulRedisConnection<String, String> redisConnection(RedisClient client) {
        return client.connect();
    }

    @Bean
    public RedisCommands<String, String> redisCommands(StatefulRedisConnection<String, String> connection) {
        return connection.sync();
    }

    @Bean
    public Cache cache(RedisCommands<String, String> redisCommands) {
        return new Cache(redisCommands);
    }

}
