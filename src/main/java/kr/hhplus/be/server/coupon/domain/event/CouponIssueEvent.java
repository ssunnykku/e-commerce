package kr.hhplus.be.server.coupon.domain.event;

import kr.hhplus.be.server.coupon.domain.vo.CouponInfo;

public record CouponIssueEvent(CouponInfo couponInfo) {

}