package hello.batch.admin.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StatisticsRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PREFIX = "statistics:";

    public void save(String type, Object value) {
        redisTemplate.opsForValue().set(PREFIX + type, value);
    }

    public <T> T get(String type, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(PREFIX + type);
        return clazz.cast(value);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
