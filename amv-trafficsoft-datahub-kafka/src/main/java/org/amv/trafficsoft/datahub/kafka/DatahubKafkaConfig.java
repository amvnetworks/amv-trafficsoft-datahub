package org.amv.trafficsoft.datahub.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.test.rule.KafkaEmbedded;

@Configuration
public class DatahubKafkaConfig {
    public static final String SENDER_TOPIC = "sender.t";
    public static final String RECEIVER_TOPIC = "receiver.t";

    @Bean
    public KafkaEmbedded embeddedKafka() {
        return new KafkaEmbedded(2, true,
                SENDER_TOPIC,
                RECEIVER_TOPIC);
    }
}
