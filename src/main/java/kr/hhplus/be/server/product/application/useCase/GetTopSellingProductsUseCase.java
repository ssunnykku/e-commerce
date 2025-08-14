package kr.hhplus.be.server.product.application.useCase;

import kr.hhplus.be.server.product.application.dto.TopSellingProductDto;
import kr.hhplus.be.server.product.infra.repository.port.OrderProductQRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class GetTopSellingProductsUseCase {
    private final OrderProductQRepository orderProductQRepository;

    private static final String CACHE_NAME = "topSellingProducts";
    private static final String CACHE_KEY = "last3Days";

    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_NAME, key = "'" + CACHE_KEY + "'") // 작은따옴표를 여기서만 사용
    public List<TopSellingProductDto> execute() {

        log.info("Executing GetTopSellingProductsUseCase.execute() - Fetching from DB...");
        return orderProductQRepository.findTop5SellingProductsLast3Days();
    }

    @Scheduled(cron = "0 0 0 * * *")
    @CacheEvict(value = CACHE_NAME, key = "'" + CACHE_KEY + "'")
    public void evictTopSellingProductsCache() {
        log.info("매일 자정 캐시 삭제 완료: {}", CACHE_NAME);

    }
}