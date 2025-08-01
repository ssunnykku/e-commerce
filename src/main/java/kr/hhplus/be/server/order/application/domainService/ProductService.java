package kr.hhplus.be.server.order.application.domainService;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.OutOfStockListException;
import kr.hhplus.be.server.common.exception.ProductNotFoundException;
import kr.hhplus.be.server.order.application.dto.OrderRequest;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> findProductsAll(Set<Long> productIds) {
        return productRepository.findAllById(productIds);
    }

    public Map<Long, Integer> getQuantitiesOfProducts(List<OrderRequest.OrderItemRequest> orderItems) {
        return orderItems.stream()
                .collect(Collectors.toMap(OrderRequest.OrderItemRequest::productId, OrderRequest.OrderItemRequest::quantity));
    }

    public Map<Product, Integer> findAndValidateProducts(Map<Long, Integer> requestedQuantities, List<Product> productList) {

        if (productList.size() != requestedQuantities.size()) {
            // 요청된 상품 ID 중 실제 존재하지 않는 상품이 있을 경우
            List<Long> foundProductIds = productList.stream().map(Product::getId).collect(Collectors.toList());
            List<Long> notFoundProductIds = requestedQuantities.keySet().stream()
                    .filter(id -> !foundProductIds.contains(id))
                    .collect(Collectors.toList());
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND, notFoundProductIds);
        }

        return productList.stream()
                .collect(Collectors.toMap(product -> product, product -> requestedQuantities.get(product.getId())));
    }

    public List<Long> findOutOfStockProduct(List<Product> productList) {
        List<Long> outOfStockProductIds = new ArrayList<>();
        for (Product product : productList) {
            // Product 재고 검증
            if (!product.hasStock()) {
                outOfStockProductIds.add(product.getId());
            }
        }

        if (!outOfStockProductIds.isEmpty()) {
            throw new OutOfStockListException(ErrorCode.PRODUCT_OUT_OF_STOCK, outOfStockProductIds);
        }
        return outOfStockProductIds;
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
