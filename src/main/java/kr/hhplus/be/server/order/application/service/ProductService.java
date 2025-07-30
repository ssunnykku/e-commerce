package kr.hhplus.be.server.order.application.service;

import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.OutOfStockListException;
import kr.hhplus.be.server.exception.ProductNotFoundException;
import kr.hhplus.be.server.order.application.dto.OrderRequest;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Map<Product, Integer> findAndValidateProducts(List<OrderRequest.OrderItemRequest> orderItems) {
        Map<Long, Integer> requestedQuantities = orderItems.stream()
                .collect(Collectors.toMap(OrderRequest.OrderItemRequest::productId, OrderRequest.OrderItemRequest::quantity));

        List<Product> products = productRepository.findAllById(requestedQuantities.keySet());

        if (products.size() != requestedQuantities.size()) {
            // 요청된 상품 ID 중 실제 존재하지 않는 상품이 있을 경우
            List<Long> foundProductIds = products.stream().map(Product::getId).collect(Collectors.toList());
            List<Long> notFoundProductIds = requestedQuantities.keySet().stream()
                    .filter(id -> !foundProductIds.contains(id))
                    .collect(Collectors.toList());
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND, notFoundProductIds);
        }

        List<Long> outOfStockProductIds = new ArrayList<>();
        for (Product product : products) {
            // Product 재고 검증
            if (!product.hasStock()) {
                outOfStockProductIds.add(product.getId());
            }
        }

        if (!outOfStockProductIds.isEmpty()) {
            throw new OutOfStockListException(ErrorCode.PRODUCT_OUT_OF_STOCK, outOfStockProductIds);
        }

        return products.stream()
                .collect(Collectors.toMap(product -> product, product -> requestedQuantities.get(product.getId())));
    }

    public long decreaseStockAndCalculatePrice(Map<Product, Integer> productsWithQuantities) {
        long totalPrice = 0;

        for (Map.Entry<Product, Integer> entry : productsWithQuantities.entrySet()) {
            Product product = entry.getKey();
            Integer quantity = entry.getValue();

            product.decreaseStock(quantity); // 수량만큼 차감
            totalPrice += product.totalPrice(quantity);

        }
        return totalPrice;
    }
}
