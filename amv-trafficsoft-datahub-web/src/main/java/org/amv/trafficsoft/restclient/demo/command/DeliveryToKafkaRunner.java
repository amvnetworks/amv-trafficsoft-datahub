package org.amv.trafficsoft.restclient.demo.command;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.reactivestreams.Publisher;
import org.springframework.boot.CommandLineRunner;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Builder
public class DeliveryToKafkaRunner implements CommandLineRunner {

    private KafkaSender<Long, DeliveryRestDto> kafkaSender;
    private Publisher<DeliveryRestDto> publisher;
    private Duration period;

    @Override
    public void run(String... args) throws Exception {

        CountDownLatch latch = new CountDownLatch(1);
        Flux<SenderRecord<Long, DeliveryRestDto, Long>> topic = Flux.interval(Duration.ofSeconds(1), period)
                .publishOn(Schedulers.immediate())
                .flatMap(i -> Flux.from(publisher))
                .map(delivery -> new ProducerRecord<>("topic", delivery.getDeliveryId(), delivery))
                .map(record -> SenderRecord.create(record, record.key()));

        kafkaSender.send(topic)
                .doOnError(e -> log.error("Send failed", e))
                .subscribeOn(Schedulers.immediate())
                .subscribe(r -> {
                            RecordMetadata metadata = r.recordMetadata();
                            log.info("Message {} sent successfully, topic-partition={}-{} offset={} timestamp={}",
                                    r.correlationMetadata(),
                                    metadata.topic(),
                                    metadata.partition(),
                                    metadata.offset(),
                                    Instant.ofEpochMilli(metadata.timestamp()).atZone(ZoneId.systemDefault()));

                            latch.countDown();
                        },
                        error -> latch.countDown(),
                        latch::countDown);

        latch.await();
    }
}
