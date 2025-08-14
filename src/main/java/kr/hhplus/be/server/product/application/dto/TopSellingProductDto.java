package kr.hhplus.be.server.product.application.dto;

public record TopSellingProductDto(
    Long productId, String name, Long price, Long stock, Integer totalQuantity // 총 판매 수
) {
    public static TopSellingProductDto of(Long productId, String name, Long price, Long stock, Integer totalQuantity){
        return new TopSellingProductDto(productId, name, price, stock, totalQuantity);
    }
}