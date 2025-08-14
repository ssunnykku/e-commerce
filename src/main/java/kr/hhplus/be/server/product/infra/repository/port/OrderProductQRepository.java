package kr.hhplus.be.server.product.infra.repository.port;

import kr.hhplus.be.server.product.application.dto.TopSellingProductDto;

import java.util.List;

public interface OrderProductQRepository {
    List<TopSellingProductDto> findTop5SellingProductsLast3Days();
}
