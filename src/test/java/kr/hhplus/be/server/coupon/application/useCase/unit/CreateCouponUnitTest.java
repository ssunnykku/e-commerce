package kr.hhplus.be.server.coupon.application.useCase.unit;

import kr.hhplus.be.server.coupon.application.dto.CouponRequest;
import kr.hhplus.be.server.coupon.application.dto.CouponResponse;
import kr.hhplus.be.server.coupon.application.useCase.CreateCouponUseCase;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCouponUseCaseTest {

    @Mock
    private CouponTypeRepository couponTypeRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private CreateCouponUseCase createCouponUseCase;

    private final Long userId = 1L;
    private final Long couponTypeId = 101L;
    private CouponRequest request;
    private LocalDate expiredAt;

    @BeforeEach
    void setUp() {
        request = CouponRequest.of(userId, couponTypeId);
        expiredAt = LocalDate.now().plusDays(30);

        // TransactionTemplate이 실제 로직을 실행하도록 모킹
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        });
    }

    @Test
    void 쿠폰발급_성공() {
        // given
        CouponType couponType = CouponType.of(couponTypeId, "10% 할인 쿠폰", 10, 30, 100, LocalDate.now());

        // 생성될 Coupon 객체
        Coupon coupon = mock(Coupon.class);
        when(coupon.getUserId()).thenReturn(userId);
        when(coupon.getId()).thenReturn(1L);
        when(coupon.getCouponTypeId()).thenReturn(couponTypeId);
        when(coupon.getDiscountRate()).thenReturn(10);
        when(coupon.getExpiresAt()).thenReturn(expiredAt);
        when(coupon.isUsed()).thenReturn(false);

        // when
        when(userRepository.findById(userId)).thenReturn(mock(User.class));
        when(couponTypeRepository.findById(couponTypeId)).thenReturn(couponType);
        when(couponRepository.findByUserIdAndCouponTypeId(userId, couponTypeId)).thenReturn(null);
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        // then
        CouponResponse response = createCouponUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.couponTypeId()).isEqualTo(couponTypeId);
        assertThat(response.discountRate()).isEqualTo(10);
        assertThat(response.isUsed()).isFalse();

        // verify
        verify(userRepository).findById(userId);
        verify(couponTypeRepository).findById(couponTypeId);
        verify(couponRepository).findByUserIdAndCouponTypeId(userId, couponTypeId);
        verify(couponRepository).save(any(Coupon.class));
    }
}
