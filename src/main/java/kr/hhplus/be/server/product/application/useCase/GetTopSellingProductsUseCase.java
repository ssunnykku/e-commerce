package kr.hhplus.be.server.product.application.useCase;

import kr.hhplus.be.server.product.application.dto.TopSellingProduct;
import kr.hhplus.be.server.product.infra.repository.port.OrderProductQRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class GetTopSellingProductsUseCase {
    private final OrderProductQRepository orderProductQRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_NAME = "CACHE:topSellingProducts";
    private static final String CACHE_KEY = "last3Days";
    private static final Duration TTL = Duration.ofDays(1); // 비즈니스 규칙: 하루 동안 유효

    @Transactional(readOnly = true)
    public List<TopSellingProduct> execute() {
        // 캐시 조회
        String redisKey = CACHE_NAME + "::" + CACHE_KEY;
        List<TopSellingProduct> cached = (List<TopSellingProduct>) redisTemplate.opsForValue().get(redisKey);

        if (cached != null) {
            return cached;
        }

        // 캐시 없으면 DB 조회 후 저장
        log.info("Fetching from DB because cache is empty...");
        List<TopSellingProduct> result = orderProductQRepository.findTop5SellingProductsLast3Days();

        redisTemplate.opsForValue().set(redisKey, result, TTL);

        return result;
    }

    // 매일 자정에 캐시 갱신
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional(readOnly = true)
    public void refreshCacheAtMidnight() {
        log.info("Refreshing top selling products cache at midnight...");

        String redisKey = CACHE_NAME + "::" + CACHE_KEY;
        List<TopSellingProduct> result = orderProductQRepository.findTop5SellingProductsLast3Days();

        redisTemplate.opsForValue().set(redisKey, result, TTL);

        log.info("Cache refreshed with {} items", result.size());
    }
}