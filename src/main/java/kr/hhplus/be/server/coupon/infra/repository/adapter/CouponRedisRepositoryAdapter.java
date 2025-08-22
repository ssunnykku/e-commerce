package kr.hhplus.be.server.coupon.infra.repository.adapter;

import kr.hhplus.be.server.coupon.infra.repository.port.CouponRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CouponRedisRepositoryAdapter implements CouponRedisRepository {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void removeKey(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public Long addSet(String key, List<String> values) {
        return redisTemplate.opsForSet().add(key, String.valueOf(values));
    }

    @Override
    public Long addSet(String key, String value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    @Override
    public Long removeSet(String key, List<String> values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    @Override
    public Long removeSet(String key, String value) {
        return redisTemplate.opsForSet().remove(key, value);
    }

    @Override
    public boolean hasTTL(String key) {
        Duration ttl = Duration.ofDays(redisTemplate.getExpire(key));
        return ttl != null && !ttl.isNegative() && !ttl.isZero();
    }

    @Override
    public void putHash(String key, String field, Object value) {
        log.debug("putHash 호출됨: key={}, field={}, value={}", key, field, value);
        redisTemplate.opsForHash().put(key, field, String.valueOf(value));
    }

    @Override
    public Long incrementHash(String key, String field, long delta) {
        try {
            return redisTemplate.opsForHash().increment(key, field, delta);
        } catch (Exception e) {
            log.error("Redis 재고 복구 실패: key={}, field={}, delta={}", key, field, delta, e);
            return null; // 또는 throw new CustomRedisException(...)
        }
    }

    @Override
    public Object getHash(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    @Override
    public boolean expire(String key, Duration duration) {
        return redisTemplate.expire(key, duration);
    }

    @Override
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    @Override
    public void put(String key, String hashKey, String value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    @Override
    public void putAll(String key, Map<String, Object> values) {
        redisTemplate.opsForHash().putAll(key, values);
    }

}

