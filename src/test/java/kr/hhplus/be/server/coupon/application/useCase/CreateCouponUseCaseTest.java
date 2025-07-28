package kr.hhplus.be.server.coupon.application.useCase;

import kr.hhplus.be.server.coupon.application.dto.CouponRequest;
import kr.hhplus.be.server.coupon.application.dto.CouponResponse;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.exception.CouponNotFoundException;
import kr.hhplus.be.server.exception.InvalidRequestException;
import kr.hhplus.be.server.exception.UserNotFoundException;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CreateCouponUseCaseTest {
    @Mock
    private CouponTypeRepository couponTypeRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserRepository userRepository;

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
        Long couponTypeId = 100L;
        CouponRequest request = CouponRequest.of(Coupon.builder()
                .userId(userId)
                .couponTypeId(couponTypeId)
                .build());

        CouponType couponType = mock(CouponType.class);
        Coupon coupon = mock(Coupon.class);
        LocalDate expiredAt = LocalDate.now().plusDays(30);

        when(couponTypeRepository.findById(couponTypeId)).thenReturn(Optional.of(couponType));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));
        when(couponRepository.findByUserIdAndCouponTypeId(userId, couponTypeId)).thenReturn(Optional.empty());
        when(couponType.calculateExpireDate()).thenReturn(expiredAt);
        when(couponType.issueTo(userId)).thenReturn(coupon);

        when(coupon.getId()).thenReturn(1L);
        when(coupon.getCouponTypeId()).thenReturn(couponTypeId);
        when(coupon.getDiscountRate()).thenReturn(10);
        when(coupon.getUsed()).thenReturn(false);
        when(coupon.getExpiresAt()).thenReturn(expiredAt);
        when(couponType.getCouponName()).thenReturn("10% 할인 쿠폰");

        when(couponRepository.save(coupon)).thenReturn(coupon);

        // when
        CouponResponse response = createCouponUseCase.execute(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.couponTypeId()).isEqualTo(couponTypeId);
        assertThat(response.couponName()).isEqualTo("10% 할인 쿠폰");
        assertThat(response.discountRate()).isEqualTo(10);
        assertThat(response.isUsed()).isFalse();

        verify(couponTypeRepository).findById(couponTypeId);
        verify(userRepository).findById(userId);
        verify(couponRepository).findByUserIdAndCouponTypeId(userId, couponTypeId);
        verify(couponType).calculateExpireDate();
        verify(couponType).issueTo(userId);
        verify(couponRepository).save(coupon);
    }

    @Test
    void 쿠폰타입이_없으면_CouponNotFoundException_발생() {
        Long userId = 1L;
        Long couponTypeId = 100L;

        when(couponTypeRepository.findById(couponTypeId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));

        CouponRequest request = CouponRequest.of(Coupon.builder()
                .userId(userId)
                .couponTypeId(couponTypeId)
                .build());

        assertThatThrownBy(() -> createCouponUseCase.execute(request))
                .isInstanceOf(CouponNotFoundException.class)
                .hasMessageContaining("쿠폰을 찾을 수 없습니다.");
    }

    @Test
    void 사용자가_없으면_UserNotFoundException_발생() {
        Long userId = 1L;
        Long couponTypeId = 100L;

        when(couponTypeRepository.findById(couponTypeId)).thenReturn(Optional.of(mock(CouponType.class)));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        CouponRequest request = CouponRequest.of(Coupon.builder()
                .userId(userId)
                .couponTypeId(couponTypeId)
                .build());

        assertThatThrownBy(() -> createCouponUseCase.execute(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }

    @Test
    void 이미_쿠폰이_발급되어있으면_InvalidRequestException_발생() {
        Long userId = 1L;
        Long couponTypeId = 100L;

        Coupon existingCoupon = mock(Coupon.class);

        when(couponTypeRepository.findById(couponTypeId)).thenReturn(Optional.of(mock(CouponType.class)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));
        when(couponRepository.findByUserIdAndCouponTypeId(userId, couponTypeId)).thenReturn(Optional.of(existingCoupon));

        CouponRequest request = CouponRequest.of(Coupon.builder()
                .userId(userId)
                .couponTypeId(couponTypeId)
                .build());

        assertThatThrownBy(() -> createCouponUseCase.execute(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("이미 발급받은 쿠폰입니다.");
    }
}
