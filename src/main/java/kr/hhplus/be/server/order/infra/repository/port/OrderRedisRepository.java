package kr.hhplus.be.server.order.infra.repository.port;

public interface OrderRedisRepository {
    void increaseScore(String key, Long productId, Integer quantity);
}
