package kr.hhplus.be.server.product.application.dto;

public record TopSellingProduct(
        Long productId, String name, Long price, Long stock, Integer totalQuantity) {

    public static TopSellingProduct of(Long productId, String name, Long price, Long stock, Integer totalQuantity){
        return new TopSellingProduct(productId, name, price, stock, totalQuantity);
    }

    public static TopSellingProduct of(Long productId, Integer totalQuantity){
        return new TopSellingProduct(productId, null, null, null, totalQuantity);
    }


}