package kr.hhplus.be.server.coupon.presentation;

import jakarta.validation.Valid;
import kr.hhplus.be.server.coupon.application.useCase.CreateCouponUseCase;
import kr.hhplus.be.server.coupon.application.dto.CouponRequest;
import kr.hhplus.be.server.coupon.application.dto.CouponResponse;
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
    private final CreateCouponUseCase createUseCase;

    @PostMapping
    public ResponseEntity<CouponResponse> issueCoupon(@RequestBody @Valid CouponRequest couponRequest) {
        CouponResponse response =  createUseCase.execute(couponRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
