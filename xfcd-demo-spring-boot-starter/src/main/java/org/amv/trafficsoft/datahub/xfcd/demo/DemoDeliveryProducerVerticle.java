package org.amv.trafficsoft.datahub.xfcd.demo;

import io.vertx.rxjava.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackageImpl;
import org.amv.trafficsoft.datahub.xfcd.XfcdEvents;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.rest.xfcd.model.NodeRestDto;
import org.amv.trafficsoft.rest.xfcd.model.ParameterRestDto;
import org.amv.trafficsoft.rest.xfcd.model.TrackRestDto;
import org.apache.commons.lang3.RandomUtils;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

@Slf4j
public class DemoDeliveryProducerVerticle extends AbstractVerticle {

    private final XfcdEvents xfcdEvents;

    public DemoDeliveryProducerVerticle(XfcdEvents xfcdEvents) {
        this.xfcdEvents = requireNonNull(xfcdEvents);
    }

    @Override
    public void start() {
        loop();
    }

    private void loop() {

        long delay = RandomUtils.nextLong(
                TimeUnit.SECONDS.toMillis(2),
                TimeUnit.SECONDS.toMillis(10));

        vertx.setTimer(100, event -> {
            log.info("emitting demo data in {}s", String.format("%.2f", delay / 1_000d));
        });

        vertx.setTimer(delay, event -> {
            emitRandomData();
            loop();
        });
    }

    private void emitRandomData() {
        xfcdEvents.publish(IncomingDeliveryEvent.class, createRandomIncomingDeliveryEvent());
    }

    private Mono<IncomingDeliveryEvent> createRandomIncomingDeliveryEvent() {
        return Mono.just(IncomingDeliveryEvent.builder()
                .deliveryPackage(TrafficsoftDeliveryPackageImpl.builder()
                        .deliveries(createRandomDeliveryRestDtos())
                        .build())
                .build());
    }

    private List<DeliveryRestDto> createRandomDeliveryRestDtos() {
        return IntStream.range(1, RandomUtils.nextInt(2, 5))
                .boxed()
                .map(foo -> createRandomDeliveryRestDto())
                .collect(Collectors.toList());
    }

    private DeliveryRestDto createRandomDeliveryRestDto() {
        long vehicleId = RandomUtils.nextLong(1, 5);

        int satellites = RandomUtils.nextInt(1, 12);
        final BigDecimal altitude = BigDecimal.valueOf(RandomUtils.nextLong(80, 700));
        final BigDecimal hdop = BigDecimal.valueOf(RandomUtils.nextLong(1, 12));
        final BigDecimal vdop = BigDecimal.valueOf(RandomUtils.nextLong(1, 12));

        return DeliveryRestDto.builder()
                .deliveryId(RandomUtils.nextLong())
                .timestamp(new Date())
                .addTrack(TrackRestDto.builder()
                        .vehicleId(vehicleId)
                        .id(RandomUtils.nextLong())
                        .addNode(NodeRestDto.builder()
                                .timestamp(new Date())
                                .altitude(altitude)
                                .hdop(hdop)
                                .heading(vdop)
                                .satellites(satellites)
                                .latitude(BigDecimal.valueOf(123.32))
                                .longitude(BigDecimal.valueOf(213.2))
                                .addXfcd(ParameterRestDto.builder()
                                        .param("kmrd")
                                        .value(String.valueOf(RandomUtils.nextLong(0L, 120_000L)))
                                        .build())
                                .addState(ParameterRestDto.builder()
                                        .param("move")
                                        .value(String.valueOf(RandomUtils.nextBoolean() ? 1 : 0))
                                        .build())
                                .build())
                        .build())
                .build();
    }
}
