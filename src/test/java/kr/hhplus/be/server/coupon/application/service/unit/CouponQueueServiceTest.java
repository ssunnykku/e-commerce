package kr.hhplus.be.server.coupon.application.service.unit;

import kr.hhplus.be.server.coupon.application.service.CouponQueueService;
import kr.hhplus.be.server.coupon.infra.publish.CouponProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CouponQueueServiceTest {

    @Mock
    private CouponProducer couponProducer;

    @InjectMocks
    private CouponQueueService couponQueueService;

    @Test
    @DisplayName("Kafka 이벤트 발행 호출 성공")
    void publishCoupon_success() {
        // given
        Long couponTypeId = 101L;
        Long userId = 1L;
        LocalDate expiresAt = LocalDate.now().plusDays(30);

        // when
        couponQueueService.publishCoupon(couponTypeId, userId, expiresAt);

        // then
        verify(couponProducer, times(1)).publishCoupon(couponTypeId, userId, expiresAt);
    }
}
