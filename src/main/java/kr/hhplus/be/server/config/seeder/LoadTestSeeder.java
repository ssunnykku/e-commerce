package kr.hhplus.be.server.config.seeder;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repository.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repository.port.CouponTypeRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

//@Component
@RequiredArgsConstructor
@Slf4j
public class LoadTestSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final CouponTypeRepository couponTypeRepository;
    private final CouponRepository couponRepository;

    private static final int TOTAL_USERS = 100;
    private static final int TOTAL_PRODUCTS = 500;
    private static final int TOTAL_ORDERS = 1000;
    private static final int TOTAL_COUPONS = 200;
    private static final int BATCH_SIZE = 100;
    private static final List<String> STATUSES = List.of("0", "1"); // ORDERED, CANCELED

    @Override
    public void run(String... args) {
        log.info("=== Load Test Seeder Start ===");
        insertUsers();
        insertProducts();
        insertCouponTypes();
        insertCoupons();
        insertOrders();
        log.info("=== Load Test Seeder Completed ===");
    }

    void insertUsers() {
        if (!userRepository.findAll().isEmpty()) return;

        List<User> batch = new ArrayList<>();
        for (int i = 1; i <= TOTAL_USERS; i++) {
            batch.add(User.of("User" + i, 100_000L));
            if (batch.size() >= BATCH_SIZE) {
                saveUsersBatch(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) saveUsersBatch(batch);
        log.info("Users created");
    }

    @Transactional
    void saveUsersBatch(List<User> batch) {
        userRepository.saveAll(batch);
    }

    void insertProducts() {
        if (!productRepository.findAll().isEmpty()) return;

        List<Product> batch = new ArrayList<>();
        for (int i = 1; i <= TOTAL_PRODUCTS; i++) {
            batch.add(Product.of("Product " + i, 5000L + (i * 10), 100L));
            if (batch.size() >= BATCH_SIZE) {
                saveProductsBatch(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) saveProductsBatch(batch);
        log.info("Products created");
    }

    @Transactional
    void saveProductsBatch(List<Product> batch) {
        productRepository.saveAll(batch);
    }

    void insertCouponTypes() {
        if (!couponTypeRepository.findAll().isEmpty()) return;

        List<CouponType> types = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            types.add(CouponType.of("CouponType " + i, 5 * i, 30, 2000));
        }
        couponTypeRepository.saveAll(types);
        log.info("Coupon Types created");
    }

    void insertCoupons() {
        if (!couponRepository.findAll().isEmpty()) return;

        List<User> users = userRepository.findAll();
        List<CouponType> types = couponTypeRepository.findAll();
        Random random = new Random();
        Set<String> issuedSet = new HashSet<>();
        List<Coupon> batch = new ArrayList<>();
        int count = 0;

        while (count < TOTAL_COUPONS) {
            User user = users.get(random.nextInt(users.size()));
            CouponType type = types.get(random.nextInt(types.size()));
            String key = user.getUserId() + "-" + type.getId();
            if (issuedSet.contains(key)) continue;
            issuedSet.add(key);

            LocalDate expiresAt = type.getCreatedAt() != null ?
                    type.getCreatedAt().plusDays(type.getValidDays()) :
                    LocalDate.now().plusDays(type.getValidDays());
            boolean used = random.nextDouble() > 0.3;

            batch.add(Coupon.of(user.getUserId(), type.getId(), expiresAt, used, type.getDiscountRate()));
            count++;

            if (batch.size() >= BATCH_SIZE) {
                saveCouponsBatch(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) saveCouponsBatch(batch);
        log.info("Coupons created");
    }

    @Transactional
    void saveCouponsBatch(List<Coupon> batch) {
        couponRepository.saveAll(batch);
    }

    void insertOrders() {
        List<User> users = userRepository.findAll();
        List<Product> products = productRepository.findAll();
        Random random = new Random();

        List<Order> ordersBatch = new ArrayList<>();
        List<OrderProduct> orderProductsBatch = new ArrayList<>();

        for (int i = 1; i <= TOTAL_ORDERS; i++) {
            User user = users.get(random.nextInt(users.size()));
            long totalAmount = 0L;
            LocalDateTime orderDate = LocalDateTime.now().minusDays(random.nextInt(90));
            String status = STATUSES.get(random.nextInt(STATUSES.size()));

            int productCount = random.nextInt(3) + 1;
            Set<Product> selectedProducts = new HashSet<>();
            while (selectedProducts.size() < productCount) {
                selectedProducts.add(products.get(random.nextInt(products.size())));
            }

            List<OrderProduct> tempProducts = new ArrayList<>();
            for (Product p : selectedProducts) {
                int qty = random.nextInt(5) + 1;
                totalAmount += p.getPrice() * qty;
                tempProducts.add(OrderProduct.of(p.getId(), null, qty, orderDate, status));
            }

            ordersBatch.add(Order.of(user.getUserId(), totalAmount, status));

            if (ordersBatch.size() >= BATCH_SIZE || i == TOTAL_ORDERS) {
                saveOrdersBatch(ordersBatch, tempProducts, orderProductsBatch);
                ordersBatch.clear();
            }

            if (i % 100 == 0) log.info(i + " orders processed");
        }
        log.info("Orders created");
    }

    @Transactional
    void saveOrdersBatch(List<Order> ordersBatch, List<OrderProduct> tempProducts, List<OrderProduct> orderProductsBatch) {
        List<Order> savedOrders = orderRepository.saveAll(ordersBatch);

        for (Order order : savedOrders) {
            for (OrderProduct op : tempProducts) {
                orderProductsBatch.add(OrderProduct.of(
                        op.getProductId(),
                        order.getId(),
                        op.getQuantity(),
                        op.getOrderDate(),
                        op.getStatus()
                ));
            }
        }

        if (!orderProductsBatch.isEmpty()) {
            orderProductRepository.saveAll(orderProductsBatch);
            orderProductsBatch.clear();
        }
    }
}