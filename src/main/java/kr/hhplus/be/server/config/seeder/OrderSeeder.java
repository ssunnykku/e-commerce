package kr.hhplus.be.server.config.seeder;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.domain.entity.OrderProduct;
import kr.hhplus.be.server.order.infra.repository.port.OrderProductRepository;
import kr.hhplus.be.server.order.infra.repository.port.OrderRepository;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

//@Component
//@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class OrderSeeder implements CommandLineRunner {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private static final int TOTAL_ORDERS = 100_000;
    private static final List<String> STATUSES = List.of("0", "1"); // ORDERED, CANCELED

    @Override
    @Transactional
    public void run(String... args) {
        log.debug("주문 더미 데이터 생성 시작");

        List<Product> products = productRepository.findAll();
        List<User> users = userRepository.findAll();

        if (products.isEmpty()) {
            List<Product> tempProducts = new ArrayList<>();
            for (int i = 1; i <= 100; i++) {
                tempProducts.add(Product.of("상품 " + i, 5_000L + (i * 100), 100L));
            }
            products = productRepository.saveAll(tempProducts);
            log.debug("상품 더미 100건 생성 완료");
        }

        if (users.isEmpty()) {
            List<User> tempUsers = new ArrayList<>();
            for (int i = 1; i <= 100; i++) {
                tempUsers.add(User.of("사용자" + i, 100_000L));
            }
            users = userRepository.saveAll(tempUsers);
            log.debug("사용자 더미 100명 생성 완료");
        }

        Random random = new Random();

        for (int i = 0; i < TOTAL_ORDERS; i++) {
            User randomUser = users.get(random.nextInt(users.size()));
            int productCount = random.nextInt(3) + 1;
            Set<Product> selectedProducts = new HashSet<>();

            while (selectedProducts.size() < productCount) {
                selectedProducts.add(products.get(random.nextInt(products.size())));
            }

            long totalAmount = 0L;
            List<OrderProduct> tempOrderProductList = new ArrayList<>();

            // 주문 날짜를 90일 내 랜덤 생성
            LocalDateTime randomOrderDate = LocalDateTime.now().minusDays(random.nextInt(90));

            // 상태값 '0' 또는 '1' 랜덤
            String status = STATUSES.get(random.nextInt(STATUSES.size()));

            for (Product product : selectedProducts) {
                int quantity = random.nextInt(5) + 1;
                long price = product.getPrice() * quantity;
                totalAmount += price;

                tempOrderProductList.add(
                        OrderProduct.of(
                                product.getId(),
                                null,
                                quantity,
                                randomOrderDate,
                                status
                        )
                );
            }

            Order order = orderRepository.save(
                    Order.of(randomUser.getUserId(), totalAmount, status)
            );

            List<OrderProduct> orderProductList = tempOrderProductList.stream()
                    .map(op -> OrderProduct.of(
                            op.getProductId(),
                            order.getId(),
                            op.getQuantity(),
                            randomOrderDate,
                            op.getStatus()
                    ))
                    .collect(Collectors.toList());

            orderProductRepository.saveAll(orderProductList);

            if (i % 10_000 == 0) {
                log.debug(i + "건 생성 완료");
            }
        }

        log.debug("주문 10만건 더미 생성 완료");
    }
}