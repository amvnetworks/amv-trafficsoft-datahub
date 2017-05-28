package org.amv.trafficsoft.datahub.xfcd.kafka;

import org.amv.trafficsoft.datahub.kafka.DatahubKafkaConfig;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Import(DatahubKafkaConfig.class)
public class XfcdKafkaConfig {

    private final KafkaEmbedded embedded;

    @Autowired
    public XfcdKafkaConfig(KafkaEmbedded embedded) {
        this.embedded = embedded;
    }

    @Bean
    public Map<String, Object> deliveryRestDtoKafkaSenderProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embedded.getBrokersAsString());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "get-data-response-delivery-producer");
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
/*
    @Bean
    public Map<String, Object> deliveryRestDtoKafkaConsumerProperties() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embedded.getBrokersAsString());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "get-data-response-delivery-receiver");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return consumerProps;
    }

    @Bean
    public ReceiverOptions<Long, DeliveryRestDto> deliveryRestDtoKafkaReceiverOptions() {
        return ReceiverOptions.<Long, DeliveryRestDto>create(deliveryRestDtoKafkaConsumerProperties())
                .subscription(Collections.singleton(DatahubKafkaConfig.SENDER_TOPIC));
    }

    @Bean
    public KafkaReceiver<Long, DeliveryRestDto> deliveryRestDtoKafkaReceiver() {
        return KafkaReceiver.create(deliveryRestDtoKafkaReceiverOptions());
    }*/
}
