package kr.hhplus.be.server.coupon.presentation;

import jakarta.validation.Valid;
import kr.hhplus.be.server.coupon.application.dto.CouponRequest;
import kr.hhplus.be.server.coupon.application.dto.CouponResponse;
import kr.hhplus.be.server.coupon.application.dto.CouponTypeRequest;
import kr.hhplus.be.server.coupon.application.dto.CouponTypeResponse;
import kr.hhplus.be.server.coupon.application.useCase.CreateCouponTypeUseCase;
import kr.hhplus.be.server.coupon.application.useCase.IssueCouponToUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class CouponController {
    private final IssueCouponToUserUseCase issueCouponToUserUseCase;
    private final CreateCouponTypeUseCase createCouponTypeUseCase;

    @PostMapping("/issue")
    public ResponseEntity<CouponResponse> issueCoupon(@RequestBody @Valid CouponRequest couponRequest) {
        CouponResponse response =  issueCouponToUserUseCase.execute(couponRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/create")
    public ResponseEntity<CouponTypeResponse> reissueCoupon(@RequestBody @Valid CouponTypeRequest couponTypeRequest) {
        CouponTypeResponse response = createCouponTypeUseCase.execute(couponTypeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
