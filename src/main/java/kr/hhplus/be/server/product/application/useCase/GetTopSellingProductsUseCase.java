package kr.hhplus.be.server.product.application.useCase;

import kr.hhplus.be.server.product.application.dto.TopSellingProductDto;
import kr.hhplus.be.server.product.infra.repository.port.OrderProductQRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTopSellingProductsUseCase {
    private final OrderProductQRepository orderProductQRepository;

    @Transactional
    public List<TopSellingProductDto> execute() {
        return orderProductQRepository.findTop5SellingProductsLast3Days();
    }
}
