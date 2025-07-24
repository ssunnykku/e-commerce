package kr.hhplus.be.server.order.infra.publish;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Log4j2
public class MockPublisherConfig {

    @Bean
    public OrderDataPublisher orderDataPublisher() {
        return orderInfo -> {
            log.info("Mock publish: " + orderInfo);
        };
    }
}