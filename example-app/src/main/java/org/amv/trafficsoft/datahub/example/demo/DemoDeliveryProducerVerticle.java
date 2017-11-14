package org.amv.trafficsoft.datahub.example.demo;

import io.vertx.rxjava.core.AbstractVerticle;
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

import static java.util.Objects.requireNonNull;

public class DemoDeliveryProducerVerticle extends AbstractVerticle {

    private final XfcdEvents xfcdEvents;

    public DemoDeliveryProducerVerticle(XfcdEvents xfcdEvents) {
        this.xfcdEvents = requireNonNull(xfcdEvents);
    }

    @Override
    public void start() {
        vertx.setPeriodic(3000, event -> {
            long vehicleId = RandomUtils.nextLong(1, 5);

            xfcdEvents.publish(IncomingDeliveryEvent.class, Mono.just(IncomingDeliveryEvent.builder()
                    .deliveryPackage(TrafficsoftDeliveryPackageImpl.builder()
                            .addDelivery(DeliveryRestDto.builder()
                                    .deliveryId(RandomUtils.nextLong())
                                    .timestamp(new Date())
                                    .addTrack(TrackRestDto.builder()
                                            .vehicleId(vehicleId)
                                            .id(RandomUtils.nextLong())
                                            .addNode(NodeRestDto.builder()
                                                    .timestamp(new Date())
                                                    .altitude(BigDecimal.ONE)
                                                    .hdop(BigDecimal.valueOf(10L))
                                                    .heading(BigDecimal.valueOf(10L))
                                                    .satellites(1)
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
                                    .build())
                            .build())
                    .build()));

        });
    }
}
