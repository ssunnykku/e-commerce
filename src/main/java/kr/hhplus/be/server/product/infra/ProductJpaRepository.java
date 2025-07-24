package kr.hhplus.be.server.product.infra;

import kr.hhplus.be.server.mock.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<Coupon, Long> {

}