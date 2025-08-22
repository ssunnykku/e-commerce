package kr.hhplus.be.server.product.infra.repository.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import kr.hhplus.be.server.product.application.dto.TopSellingProduct;
import kr.hhplus.be.server.product.infra.repository.port.ProductRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRedisRepositoryAdapter implements ProductRedisRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private ZSetOperations<String, Object> zSetOps;


    private static final String DAILY_RANKING_PREFIX = "CACHE:ranking:products:*";
    private static final String TOP_SELLING_CACHE_PREFIX = "CACHE:topSellingProducts*";

    @Override
    public void deleteAll() {
        Set<String> keysToDelete = new HashSet<>();

        keysToDelete.addAll(redisTemplate.keys(DAILY_RANKING_PREFIX));
        keysToDelete.addAll(redisTemplate.keys(TOP_SELLING_CACHE_PREFIX));

        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
            log.info("Deleted {} keys from Redis.", keysToDelete.size());
        }
    }

    @PostConstruct
    private void init() {
        zSetOps = redisTemplate.opsForZSet();
    }

    @Override
    public void addToZSet(String key, String member, double score) {
        zSetOps.add(key, member, score);
    }

    @Override
    public List<TopSellingProduct> getTopSellingProducts(String key) {
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached instanceof List<?>) {
            List<?> rawList = (List<?>) cached;
            ObjectMapper mapper = new ObjectMapper();

            List<TopSellingProduct> products = rawList.stream()
                    .map(obj -> mapper.convertValue(obj, TopSellingProduct.class))
                    .collect(Collectors.toList());

            return products;
        }
        return null;

    }

    @Override
    public void setTopSellingProducts(String key, List<TopSellingProduct> products, Duration ttl) {
        redisTemplate.opsForValue().set(key, products, ttl);
    }

    @Override
    public Long unionAndStore(String key, List<String> otherKeys, String destKey) {
        return redisTemplate.opsForZSet().unionAndStore(key, otherKeys, destKey);
    }

    @Override
    public void setExpire(String key, Duration duration) {
        redisTemplate.expire(key, duration);
    }

    @Override
    public List<ZSetOperations.TypedTuple<Object>> reverseRangeWithScores(String key, long start, long end) {
        Set<ZSetOperations.TypedTuple<Object>> sortedSet = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        if (sortedSet == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(sortedSet);    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void removeKey(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void addRankingScore(String key, Long productId, double score) {
        redisTemplate.opsForZSet().add(key, productId.toString(), score);
    }

}
