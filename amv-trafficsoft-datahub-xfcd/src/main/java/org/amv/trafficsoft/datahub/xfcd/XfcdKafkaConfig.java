package org.amv.trafficsoft.datahub.xfcd;

import io.swagger.annotations.Authorization;
import org.amv.trafficsoft.datahub.kafka.KafkaConfig;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Import(KafkaConfig.class)
public class XfcdKafkaConfig {

    //private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC = "demo-topic";

    private final KafkaEmbedded embedded;

    @Autowired
    public XfcdKafkaConfig(KafkaEmbedded embedded) {
        this.embedded = embedded;
    }

    @Bean
    public Map<String, Object> deliveryRestDtoKafkaSenderProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embedded.getBrokersAsString());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "sample-producer");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    @Bean
    public SenderOptions<Long, DeliveryRestDto> deliveryRestDtoKafkaSenderOptions() {
        return SenderOptions.create(deliveryRestDtoKafkaSenderProperties());
    }

    @Bean
    public KafkaSender<Long, DeliveryRestDto> deliveryRestDtoKafkaSender() {
        return KafkaSender.create(deliveryRestDtoKafkaSenderOptions());
    }
}
