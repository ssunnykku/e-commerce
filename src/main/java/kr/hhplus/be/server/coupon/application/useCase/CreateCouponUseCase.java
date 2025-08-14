package kr.hhplus.be.server.coupon.application.useCase;

import kr.hhplus.be.server.common.aop.DistributedLock;
import kr.hhplus.be.server.coupon.application.dto.CouponRequest;
import kr.hhplus.be.server.coupon.application.dto.CouponResponse;
import kr.hhplus.be.server.coupon.application.service.CreateCouponService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateCouponUseCase {
    private final RedissonClient redissonClient;
    private final CreateCouponService createCouponService;

    @DistributedLock(domain = "couponType", key = "#request.couponTypeId")
    public CouponResponse execute(CouponRequest request) {
            return createCouponService.executeInTransaction(request);
        }
    }

