package kr.hhplus.be.server.product.application.useCase;

import kr.hhplus.be.server.product.application.dto.TopSellingProduct;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRedisRepository;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopSellingProductsUseCase {
    private final ProductRedisRepository productRedisRepository;
    private final ProductRepository productRepository;

    private static final String DAILY_RANKING_PREFIX = "CACHE:ranking:products:";
    private static final String TOP_SELLING_CACHE_NAME = "CACHE:topSellingProducts";
    private static final String TOP_SELLING_CACHE_KEY = "last3Days";

    private static final Duration TTL = Duration.ofDays(1); // 비즈니스 규칙: 하루 동안 유효
    private static final int TOP_N_COUNT = 5;

    String getTopSellingKey() {
        return TOP_SELLING_CACHE_NAME + "::" + TOP_SELLING_CACHE_KEY;
    }

    String getDailyRankingKey(LocalDate date) {
        return DAILY_RANKING_PREFIX + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    String get3DaysRankingKey() {
        return DAILY_RANKING_PREFIX + "3days" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    @Transactional(readOnly = true)
    public List<TopSellingProduct> execute() {
        // 캐시 조회
        String redisKey = getTopSellingKey();

        List<TopSellingProduct> cached = productRedisRepository.getTopSellingProducts(redisKey);

        if (cached != null) {
            return cached;
        }

        log.info("Fetching from DB because cache is empty...");

        List<TopSellingProduct> result = getData();
        if (!result.isEmpty()) {
            productRedisRepository.setTopSellingProducts(getTopSellingKey(), result, TTL);
        }
        return result;
    }


    // 매일 자정에 캐시 갱신
    @Scheduled(cron = "0 0 0 * * *")
    public void generateRanking() {
        LocalDate today = LocalDate.now();

        // 3일간의 키
        List<String> dailyKeys = List.of(
                getDailyRankingKey(today.minusDays(2)),
                getDailyRankingKey(today.minusDays(1)),
                getDailyRankingKey(today)
        );

        String unionKey = get3DaysRankingKey();

        // 집계, get3DaysRankingKey()로 저장
        productRedisRepository.unionAndStore(
                dailyKeys.get(0),    // 기준 키
                dailyKeys.subList(1, dailyKeys.size()), // 나머지 키들
                unionKey
        );

        // 3일치 합산 데이터는 25시간 유지
        productRedisRepository.setExpire(unionKey, Duration.ofHours(25));

        List<TopSellingProduct> result = getData();

        if (!result.isEmpty()) {
            productRedisRepository.setTopSellingProducts(getTopSellingKey(), result, TTL);
            productRedisRepository.setExpire(getTopSellingKey(), Duration.ofDays(3));
        }

        log.info("Cache refreshed with {} items", result.size());

    }

    private List<TopSellingProduct> getData() {
        List<ZSetOperations.TypedTuple<Object>> rankings = productRedisRepository.reverseRangeWithScores(get3DaysRankingKey(), 0, TOP_N_COUNT - 1);

        // 1. Redis에서 가져온 랭킹 데이터를 Map으로 변환 (productId -> score)
        Map<Long, Integer> rankingScores = rankings.stream()
                .collect(Collectors.toMap(
                        tuple -> Long.parseLong(String.valueOf(tuple.getValue())),
                        tuple -> tuple.getScore().intValue()
                ));

        // 2. Map의 KeySet을 사용하여 DB에서 상품 정보를 한 번에 조회
        List<Product> products = productRepository.findAllById(rankingScores.keySet());

        // 3. DB에서 가져온 상품 리스트와 Redis의 랭킹 데이터를 결합
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 4. 원래 Redis 랭킹의 순서대로 결과 리스트 생성
        return rankings.stream()
                .map(tuple -> {
                    Long productId = Long.parseLong(String.valueOf(tuple.getValue()));
                    Product product = productMap.get(productId);

                    // 상품 정보가 있다면 DTO 생성
                    if (product != null) {
                        return TopSellingProduct.of(
                                product.getId(),
                                product.getName(),
                                product.getPrice(),
                                product.getStock(),
                                tuple.getScore().intValue()
                        );
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
