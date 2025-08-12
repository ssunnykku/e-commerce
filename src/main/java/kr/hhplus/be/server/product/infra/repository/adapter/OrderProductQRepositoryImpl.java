package kr.hhplus.be.server.product.infra.repository.adapter;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.hhplus.be.server.order.domain.entity.QOrderProduct;
import kr.hhplus.be.server.product.application.dto.TopSellingProductDto;
import kr.hhplus.be.server.product.domain.entity.QProduct;
import kr.hhplus.be.server.product.infra.repository.port.OrderProductQRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderProductQRepositoryImpl implements OrderProductQRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<TopSellingProductDto> findTop5SellingProductsLast3Days() {
        QOrderProduct orderProduct = QOrderProduct.orderProduct;
        QProduct product = QProduct.product;

        return queryFactory
                .select(
                        Projections.constructor(
                                TopSellingProductDto.class,
                                orderProduct.productId,
                                product.name,
                                product.price,
                                product.stock,
                                orderProduct.quantity.sum()
                        )
                )
                .from(orderProduct)
                .join(orderProduct.product, product)
                .where(
                        orderProduct.orderDate.goe(LocalDateTime.now().minusDays(3)),
                        orderProduct.status.eq("1")
                )
                .groupBy(orderProduct.productId, product.name, product.price, product.stock)
                .orderBy(orderProduct.quantity.sum().desc())
                .limit(5)
                .fetch();
    }
}

