package kr.hhplus.be.server.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic orderPaymentTopic() {
        return TopicBuilder.name("trace-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic couponIssueTopic() {
        return TopicBuilder.name("coupon-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic dlqTopic() {
        return TopicBuilder.name("DLQ-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

}