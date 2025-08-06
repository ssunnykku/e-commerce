package kr.hhplus.be.server.user.infra.reposistory.adapter;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.UserNotFoundException;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class UserRepositoryAdapterTest {
    @Mock
    private CouponTypeRepository couponTypeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserRepositoryAdapter userRepositoryAdapter;

    @Test
    void 사용자가_없으면_UserNotFoundException_발생() {
        Long userId = 1L;
        Long couponTypeId = 100L;

        when(couponTypeRepository.findByIdLock(couponTypeId)).thenReturn(Optional.of(mock(CouponType.class)));
        when(userRepository.findById(userId));

        assertThatThrownBy(() -> userRepositoryAdapter.findById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

}