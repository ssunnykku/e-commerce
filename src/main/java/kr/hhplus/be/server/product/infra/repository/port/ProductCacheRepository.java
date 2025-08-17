package kr.hhplus.be.server.product.infra.repository.port;

import kr.hhplus.be.server.product.application.dto.TopSellingProduct;

import java.time.Duration;
import java.util.List;

public interface ProductCacheRepository {
    List<TopSellingProduct> getTopSellingProducts(String key);
    void setTopSellingProducts(String key, List<TopSellingProduct> products, Duration ttl);
}