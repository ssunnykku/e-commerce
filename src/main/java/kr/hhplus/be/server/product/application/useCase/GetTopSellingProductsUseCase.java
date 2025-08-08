package kr.hhplus.be.server.product.application.useCase;

import kr.hhplus.be.server.product.application.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTopSellingProductsUseCase {

    @Transactional
    public List<ProductResponse> execute(Long id) {
        return null;
    }
}
