package kr.hhplus.be.server.config;

public final class KafkaTopics {

    public static final String TRACE_TOPIC = "trace-topic";
    public static final String COUPON_TOPIC = "coupon-topic";
    public static final String DLQ_TOPIC = "DLQ-topic";

    private KafkaTopics() {
    }
}
