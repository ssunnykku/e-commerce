package kr.hhplus.be.server.product.infra.repository.port;

import kr.hhplus.be.server.product.application.dto.TopSellingProduct;

import java.util.List;

public interface OrderProductQRepository {
    List<TopSellingProduct> findTop5SellingProductsLast3Days();
}
