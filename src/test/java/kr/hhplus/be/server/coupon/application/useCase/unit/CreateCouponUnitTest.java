package kr.hhplus.be.server.coupon.application.useCase.unit;

import kr.hhplus.be.server.coupon.application.dto.CouponRequest;
import kr.hhplus.be.server.coupon.application.dto.CouponResponse;
import kr.hhplus.be.server.coupon.application.useCase.CreateCouponUseCase;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repositpry.CouponTypeJpaRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCouponUnitTest {
    @Mock
    private CouponTypeRepository couponTypeRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CouponTypeJpaRepository couponTypeJpaRepository;

    @InjectMocks
    private CreateCouponUseCase createCouponUseCase;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 쿠폰발급_성공() {
        // given
        Long userId = 1L;
        Long couponTypeId = 101L;
        CouponRequest request = CouponRequest.of(userId, couponTypeId);
        LocalDate expiredAt = LocalDate.now().plusDays(30);

        CouponType couponType = mock(CouponType.class);
        Coupon coupon = mock(Coupon.class);

        when(couponTypeRepository.findByIdLock(couponTypeId)).thenReturn(couponType);
        when(userRepository.findById(userId)).thenReturn(mock(User.class));
        when(couponRepository.findByUserIdAndCouponTypeId(userId, couponTypeId)).thenReturn(null);
        when(couponType.getId()).thenReturn(couponTypeId);
        when(couponType.calculateExpireDate()).thenReturn(expiredAt);
        when(couponType.issueTo(userId)).thenReturn(coupon);
        when(couponType.getCouponName()).thenReturn("10% 할인 쿠폰");


        when(coupon.getId()).thenReturn(1L);
        when(coupon.getCouponTypeId()).thenReturn(couponTypeId);
        when(coupon.getDiscountRate()).thenReturn(10);
        when(coupon.getUsed()).thenReturn(false);
        when(coupon.getExpiresAt()).thenReturn(expiredAt);

        when(couponRepository.save(coupon)).thenReturn(coupon);

        // when
        CouponResponse response = createCouponUseCase.execute(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.couponTypeId()).isEqualTo(couponTypeId);
        assertThat(response.discountRate()).isEqualTo(10);
        assertThat(response.isUsed()).isFalse();

        verify(userRepository).findById(userId);
        verify(couponRepository).findByUserIdAndCouponTypeId(userId, couponTypeId);
        verify(couponTypeRepository).findByIdLock(couponTypeId);
        verify(couponType).calculateExpireDate();
        verify(couponType).issueTo(userId);
        verify(couponRepository).save(coupon);
    }

}
