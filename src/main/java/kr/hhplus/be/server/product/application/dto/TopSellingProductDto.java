package kr.hhplus.be.server.product.application.dto;

import java.io.Serializable;

public record TopSellingProductDto(
        Long productId, String name, Long price, Long stock, Integer totalQuantity
) implements Serializable {

    public static TopSellingProductDto of(Long productId, String name, Long price, Long stock, Integer totalQuantity){
        return new TopSellingProductDto(productId, name, price, stock, totalQuantity);
    }
}