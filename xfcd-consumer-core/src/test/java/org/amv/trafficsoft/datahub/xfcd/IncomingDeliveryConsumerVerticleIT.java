package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmableDeliveryEvent;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDtoMother;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@RunWith(VertxUnitRunner.class)
public class IncomingDeliveryConsumerVerticleIT {

    private Vertx vertx;

    private DeliveryConsumer dao;

    private XfcdEvents xfcdEvents;

    @Before
    public void setUp(TestContext context) throws IOException {
        this.vertx = Vertx.vertx(new VertxOptions()
                .setEventLoopPoolSize(1)
                .setInternalBlockingPoolSize(1));

        this.dao = spy(DeliveryConsumer.class);
        this.xfcdEvents = new XfcdEvents(vertx);

        final IncomingDeliveryConsumerVerticle sut = IncomingDeliveryConsumerVerticle.builder()
                .xfcdEvents(xfcdEvents)
                .incomingDeliveryEventConsumer(ConfirmingDeliveryConsumer.builder()
                        .deliveryConsumer(this.dao)
                        .confirmDelivery(true)
                        .build())
                .build();

        vertx.deployVerticle(sut, context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void itShouldDoNothingOnEmptyDelivery(TestContext context) throws Exception {
        verifyNoMoreInteractions(dao);

        final TrafficsoftDeliveryPackage deliveryPackage = TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(Collections.emptyList())
                .build();

        Async async = context.async();
        xfcdEvents.subscribe(IncomingDeliveryEvent.class, new BaseSubscriber<IncomingDeliveryEvent>() {
            @Override
            protected void hookOnNext(IncomingDeliveryEvent value) {
                vertx.setTimer(TimeUnit.SECONDS.toMillis(1), i -> async.complete());
            }
        });

        xfcdEvents.publish(IncomingDeliveryEvent.class, Mono.just(IncomingDeliveryEvent.builder()
                .deliveryPackage(deliveryPackage)
                .build()));

        async.await(TimeUnit.SECONDS.toMillis(10));

        verifyZeroInteractions(dao);
    }

    @Test
    public void itShouldCallConsumerOnIncomingDelivery(TestContext context) throws Exception {
        final TrafficsoftDeliveryPackageImpl deliveryPackage = TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(DeliveryRestDtoMother.randomList())
                .build();

        Async async = context.async();

        xfcdEvents.subscribe(IncomingDeliveryEvent.class, new BaseSubscriber<IncomingDeliveryEvent>() {
            @Override
            protected void hookOnNext(IncomingDeliveryEvent value) {
                vertx.setTimer(TimeUnit.SECONDS.toMillis(1), i -> async.complete());
            }
        });

        xfcdEvents.publish(IncomingDeliveryEvent.class, Mono.just(IncomingDeliveryEvent.builder()
                .deliveryPackage(deliveryPackage)
                .build()));

        async.await(TimeUnit.SECONDS.toMillis(10));

        verify(dao, times(1)).consume(eq(deliveryPackage));
    }

    @Test
    public void itShouldCallSendConfirmableDeliveryEvent(TestContext context) throws Exception {
        final TrafficsoftDeliveryPackageImpl deliveryPackage = TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(DeliveryRestDtoMother.randomList())
                .build();

        Async async = context.async();

        xfcdEvents.subscribe(ConfirmableDeliveryEvent.class, new BaseSubscriber<ConfirmableDeliveryEvent>() {
            @Override
            protected void hookOnNext(ConfirmableDeliveryEvent value) {
                vertx.setTimer(TimeUnit.SECONDS.toMillis(1), i -> async.complete());
            }
        });

        xfcdEvents.publish(IncomingDeliveryEvent.class, Mono.just(IncomingDeliveryEvent.builder()
                .deliveryPackage(deliveryPackage)
                .build()));

        async.await(TimeUnit.SECONDS.toMillis(10));

        verify(dao, times(1)).consume(eq(deliveryPackage));
    }
}
