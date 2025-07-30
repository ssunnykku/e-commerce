package kr.hhplus.be.server.coupon.application.useCase.Integration;

import kr.hhplus.be.server.coupon.application.dto.CouponRequest;
import kr.hhplus.be.server.coupon.application.dto.CouponResponse;
import kr.hhplus.be.server.coupon.application.useCase.CreateCouponUseCase;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.utility.TestcontainersConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CreateCouponUseCaseTest {
    @Autowired
    private CreateCouponUseCase createCouponUseCase;
    @Autowired
    private CouponTypeRepository couponTypeRepository;
    @Autowired
    private UserRepository userRepository;

    private User user;
    private CouponType couponType;

    @BeforeEach
    void setUp() {
        user = User.of("sun", 20000L);
        userRepository.save(user);

        couponType = couponTypeRepository.save(CouponType.of("10% 할인 쿠폰", 10, 20, 100L));
    }

    @Test
    @DisplayName("사용자에게 쿠폰을 발급한다.")
    void 쿠폰_생성() {
        //given
        CouponRequest request = CouponRequest.of(Coupon.of(user.getUserId(), couponType.getId()));
        //when
        CouponResponse response = createCouponUseCase.execute(request);
        //then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(user.getUserId());
        assertThat(response.couponTypeId()).isEqualTo(couponType.getId());
        assertThat(response.discountRate()).isEqualTo(couponType.getDiscountRate());
        assertThat(response.isUsed()).isFalse();

    }
}