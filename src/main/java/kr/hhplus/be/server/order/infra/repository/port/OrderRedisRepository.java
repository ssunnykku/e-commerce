package kr.hhplus.be.server.order.infra.repository.port;

import java.time.Duration;

public interface OrderRedisRepository {
    void increaseScore(String key, Long productId, Integer quantity);
    void setExpire(String key, Duration duration);
    boolean keyExists(String key);
}
