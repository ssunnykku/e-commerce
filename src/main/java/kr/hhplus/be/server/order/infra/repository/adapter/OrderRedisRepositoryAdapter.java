package kr.hhplus.be.server.order.infra.repository.adapter;

import kr.hhplus.be.server.order.infra.repository.port.OrderRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRedisRepositoryAdapter implements OrderRedisRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void increaseScore(String key, Long productId, Integer quantity) {
        redisTemplate.opsForZSet().incrementScore(key, productId, quantity);
    }
}
