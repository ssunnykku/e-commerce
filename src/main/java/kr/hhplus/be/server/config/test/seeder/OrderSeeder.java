package kr.hhplus.be.server.config.test.seeder;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.domain.entity.OrderProduct;
import kr.hhplus.be.server.order.domain.entity.OrderStatus;
import kr.hhplus.be.server.order.infra.repository.port.OrderProductRepository;
import kr.hhplus.be.server.order.infra.repository.port.OrderRepository;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;

import java.util.*;
import java.util.stream.Collectors;

//@Component
//@Profile("local")
@RequiredArgsConstructor
public class OrderSeeder implements CommandLineRunner {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private static final int TOTAL_ORDERS = 100_000;

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("주문 더미 데이터 생성 시작");

        List<Product> products = productRepository.findAll();
        List<User> users = userRepository.findAll();

        if (products.isEmpty()) {
            List<Product> tempProducts = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                tempProducts.add(Product.of("상품 " + i, 10_000L * i, 100L));
            }
            products = productRepository.saveAll(tempProducts);
            System.out.println("상품 더미 10건 생성 완료");
        }

        if (users.isEmpty()) {
            List<User> tempUsers = new ArrayList<>();
            for (int i = 1; i <= 100; i++) {
                tempUsers.add(User.of("사용자" + i, 100_000L));
            }
            users = userRepository.saveAll(tempUsers);
            System.out.println("사용자 더미 100명 생성 완료");
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

            for (Product product : selectedProducts) {
                int quantity = random.nextInt(5) + 1;
                long price = product.getPrice() * quantity;
                totalAmount += price;

                tempOrderProductList.add(
                        OrderProduct.of(
                                product.getId(),
                                null,
                                quantity,
                                null,
                                OrderStatus.ORDERED.getCode()
                        )
                );
            }

            Order order = orderRepository.save(
                    Order.of(randomUser.getUserId(), totalAmount, OrderStatus.ORDERED.getCode())
            );

            List<OrderProduct> orderProductList = tempOrderProductList.stream()
                    .map(op -> OrderProduct.of(
                            op.getProductId(),
                            order.getId(),
                            op.getQuantity(),
                            order.getOrderDate(),
                            op.getStatus()
                    ))
                    .collect(Collectors.toList());

            orderProductRepository.saveAll(orderProductList);

        }

        System.out.println("주문 10만건 더미 생성 완료");
    }
}