package kr.hhplus.be.server.product.application.dto;

import kr.hhplus.be.server.product.domain.entity.Product;

public record ProductResponse(Long id, String name, Integer price, Integer stock) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getPrice(), product.getStock());
    }
}