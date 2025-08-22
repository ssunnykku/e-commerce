package kr.hhplus.be.server.product.infra.repository.port;

import kr.hhplus.be.server.product.application.dto.TopSellingProduct;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
import java.util.List;

public interface ProductRedisRepository {
    void deleteAll();
    void addToZSet(String key, String member, double score);
    List<TopSellingProduct> getTopSellingProducts(String key);
    void setTopSellingProducts(String key, List<TopSellingProduct> products, Duration ttl);
    // 3일치 합산
    Long unionAndStore(String key, List<String> otherKeys, String destKey);
    void setExpire(String key, Duration duration);
    // 상위 랭킹을 조회
    List<ZSetOperations.TypedTuple<Object>> reverseRangeWithScores(String key, long start, long end);
    void delete(String key);
    void removeKey(String key);
    void addRankingScore(String key, Long productId, double score);

}