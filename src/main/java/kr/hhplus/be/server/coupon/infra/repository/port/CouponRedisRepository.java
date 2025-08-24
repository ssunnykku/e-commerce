package kr.hhplus.be.server.coupon.infra.repository.port;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public interface CouponRedisRepository {
    void removeKey(String key);
    Long addSet(String key, List<String> values);
    Long addSet(String key, String value);
    Long removeSet(String key, List<String> values);
    Long removeSet(String key, String value);
    boolean hasTTL(String key);
    void putHash(String key, String field, Object value);
    Long incrementHash(String key, String field, long delta);
    Object getHash(String key, String field);
    boolean expire(String key, Duration duration);
    Long getExpire(String key);
    void put(String key, String hashKey, String value);
    void putAll(String key, Map<String, Object> values);
}
