package kr.hhplus.be.server.product.infra.repository.adapter;

import kr.hhplus.be.server.product.application.dto.TopSellingProduct;
import kr.hhplus.be.server.product.infra.repository.port.ProductCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductCacheRepositoryImpl implements ProductCacheRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<TopSellingProduct> getTopSellingProducts(String key) {
        return (List<TopSellingProduct>) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void setTopSellingProducts(String key, List<TopSellingProduct> products, Duration ttl) {
        redisTemplate.opsForValue().set(key, products, ttl);
    }
}
