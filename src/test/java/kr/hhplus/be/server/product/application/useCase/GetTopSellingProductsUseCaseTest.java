package kr.hhplus.be.server.product.application.useCase;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.utility.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class GetTopSellingProductsUseCaseTest {
    @Autowired
    private GetTopSellingProductsUseCase useCase;

    @Test
    public void testCache() {
        useCase.execute();
        useCase.execute();
    }
}