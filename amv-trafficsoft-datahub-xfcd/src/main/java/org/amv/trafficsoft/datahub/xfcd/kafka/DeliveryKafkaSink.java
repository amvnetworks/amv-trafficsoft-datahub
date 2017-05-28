package org.amv.trafficsoft.datahub.xfcd.kafka;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.kafka.DatahubKafkaConfig;
import org.amv.trafficsoft.datahub.xfcd.XfcdDeliveryFluxSink;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import java.time.Instant;
import java.time.ZoneId;

@Builder
@Slf4j
public class DeliveryKafkaSink extends BaseSubscriber<DeliveryRestDto>
        implements XfcdDeliveryFluxSink {

    private KafkaSender<Long, DeliveryRestDto> kafkaSender;
    private String topic;

    @Override
    protected void hookOnNext(DeliveryRestDto value) {
        Mono<SenderRecord<Long, DeliveryRestDto, Long>> records = Mono.fromCallable(() -> value)
                .map(delivery -> new ProducerRecord<>(DatahubKafkaConfig.SENDER_TOPIC, delivery.getDeliveryId(), delivery))
                .map(record -> SenderRecord.create(record, record.key()));

        kafkaSender.send(records)
                .doOnError(e -> log.error("Send failed", e))
                .subscribe(this::logRecord,
                        error -> log.error("", error),
                        () -> log.info("completed")
                );
    }

    private void logRecord(SenderResult<Long> r) {
        if(r.exception() != null) {
            log.error("", r.exception());
            return;
        }

        RecordMetadata metadata = r.recordMetadata();
        log.info("Message {} sent successfully, topic-partition={}-{} offset={} timestamp={}",
                r.correlationMetadata(),
                metadata.topic(),
                metadata.partition(),
                metadata.offset(),
                Instant.ofEpochMilli(metadata.timestamp()).atZone(ZoneId.systemDefault()));
    }

}
