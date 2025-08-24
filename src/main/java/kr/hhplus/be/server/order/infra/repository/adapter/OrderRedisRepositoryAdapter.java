package kr.hhplus.be.server.order.infra.repository.adapter;

import kr.hhplus.be.server.order.infra.repository.port.OrderRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class OrderRedisRepositoryAdapter implements OrderRedisRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void increaseScore(String key, Long productId, Integer quantity) {
        redisTemplate.opsForZSet().incrementScore(key, productId, quantity);
    }

    @Override
    public void setExpire(String key, Duration duration) {
        redisTemplate.expire(key, duration);
    }

    @Override
    public boolean keyExists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

}
