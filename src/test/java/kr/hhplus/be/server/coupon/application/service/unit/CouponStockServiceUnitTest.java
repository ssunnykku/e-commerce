package kr.hhplus.be.server.coupon.application.service.unit;

import kr.hhplus.be.server.common.exception.InvalidRequestException;
import kr.hhplus.be.server.coupon.application.service.CouponStockService;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repository.port.CouponRedisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponStockServiceUnitTest {
    @Mock
    private CouponRedisRepository couponRedisRepository;

    @InjectMocks
    private CouponStockService couponStockService;

    private CouponType couponType;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        couponType = CouponType.of(101L, "10% 할인 쿠폰", 10, 30, 100, LocalDate.now());
    }

    @Test
    @DisplayName("재고 차감 성공")
    void checkAndDecreaseStock_success() {
        // given
        when(couponRedisRepository.addSet(anyString(), anyString())).thenReturn(1L);
        when(couponRedisRepository.hasTTL(anyString())).thenReturn(true);
        when(couponRedisRepository.incrementHash(anyString(), eq("stock"), eq(-1L)))
                .thenReturn(99L);

        // when
        couponStockService.checkAndDecreaseStock(userId, couponType);

        // then
        verify(couponRedisRepository).incrementHash(anyString(), eq("stock"), eq(-1L));
    }

    @Test
    @DisplayName("중복 발급 시 예외 발생")
    void checkAndDecreaseStock_duplicateUser() {
        // given
        when(couponRedisRepository.addSet(anyString(), anyString())).thenReturn(0L);

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> couponStockService.checkAndDecreaseStock(userId, couponType));
    }

    @Test
    @DisplayName("재고 부족 시 예외 발생")
    void checkAndDecreaseStock_outOfStock() {
        // given
        when(couponRedisRepository.addSet(anyString(), anyString())).thenReturn(1L);
        when(couponRedisRepository.hasTTL(anyString())).thenReturn(true);
        when(couponRedisRepository.incrementHash(anyString(), eq("stock"), eq(-1L)))
                .thenReturn(-1L);

        // when & then
        assertThrows(InvalidRequestException.class,
                () -> couponStockService.checkAndDecreaseStock(userId, couponType));
    }
}