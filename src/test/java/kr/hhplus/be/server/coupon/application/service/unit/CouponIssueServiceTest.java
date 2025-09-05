package kr.hhplus.be.server.coupon.application.service.unit;

import kr.hhplus.be.server.coupon.application.service.CouponIssueService;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repository.port.CouponTypeRepository;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponIssueServiceTest {

    @Mock
    private CouponTypeRepository couponTypeRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CouponIssueService couponIssueService;

    private CouponType couponType;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        couponType = CouponType.of(101L, "10% 할인 쿠폰", 10, 30, 100, LocalDate.now());
    }

    @Test
    @DisplayName("쿠폰 발급 성공")
    void createCouponForUser_success() {
        // given
        when(couponTypeRepository.findById(couponType.getId())).thenReturn(couponType);

        // when
        Coupon coupon = couponIssueService.createCouponForUser(
                couponType.getId(),
                userId,
                LocalDate.now().plusDays(30)
        );

        // then
        assertThat(coupon).isNotNull();
        assertThat(coupon.getUserId()).isEqualTo(userId);
        assertThat(coupon.getCouponTypeId()).isEqualTo(couponType.getId());
    }

    @Test
    @DisplayName("쿠폰타입 조회 시 UserRepository도 호출된다")
    void findCouponType_success() {
        // given
        when(couponTypeRepository.findById(couponType.getId())).thenReturn(couponType);

        // when
        CouponType result = couponIssueService.findCouponType(userId, couponType.getId());

        // then
        verify(userRepository).findById(userId);
        assertThat(result).isEqualTo(couponType);
    }
}