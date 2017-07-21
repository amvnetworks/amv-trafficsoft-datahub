package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.core.Vertx;
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

import static org.mockito.Mockito.*;

@RunWith(VertxUnitRunner.class)
public class TrafficsoftDeliveryJdbcVerticleIT {

    private Vertx vertx;

    private XfcdDataStore dao;

    private XfcdEvents xfcdEvents;

    @Before
    public void setUp(TestContext context) throws IOException {
        this.vertx = Vertx.vertx();
        this.dao = spy(XfcdDataStore.class);
        this.xfcdEvents = new XfcdEvents(vertx);

        final DeliveryDataStoreVerticle sut = DeliveryDataStoreVerticle.builder()
                .xfcdEvents(xfcdEvents)
                .dataStore(dao)
                .build();

        vertx.deployVerticle(sut, context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void itShouldDoNothingOnEmptyList(TestContext context) throws Exception {
        verifyNoMoreInteractions(dao);

        final TrafficsoftDeliveryPackage deliveryPackage = TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(Collections.emptyList())
                .build();

        Async async = context.async();
        xfcdEvents.subscribe(IncomingDeliveryEvent.class, new BaseSubscriber<IncomingDeliveryEvent>() {
            @Override
            protected void hookOnNext(IncomingDeliveryEvent value) {
                async.complete();
            }
        });

        xfcdEvents.publish(IncomingDeliveryEvent.class, Mono.just(IncomingDeliveryEvent.builder()
                .deliveryPackage(deliveryPackage)
                .build()));

        async.await();
    }

    @Test
    public void itShouldCallSaveActionOnDelivery(TestContext context) throws Exception {
        final TrafficsoftDeliveryPackageImpl deliveryPackage = TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(DeliveryRestDtoMother.randomList())
                .build();

        Async async = context.async();

        xfcdEvents.subscribe(ConfirmableDeliveryEvent.class, new BaseSubscriber<ConfirmableDeliveryEvent>() {
            @Override
            protected void hookOnNext(ConfirmableDeliveryEvent value) {
                async.complete();
            }
        });

        xfcdEvents.publish(IncomingDeliveryEvent.class, Mono.just(IncomingDeliveryEvent.builder()
                .deliveryPackage(deliveryPackage)
                .build()));

        async.await();

        verify(dao, times(1)).save(eq(deliveryPackage));
    }

    @Test
    public void itShouldCallSendConfirmableDeliveryEventIfItHoldsPrimaryDataStore(TestContext context) throws Exception {
        when(this.dao.isPrimaryDataStore()).thenReturn(true);

        final TrafficsoftDeliveryPackageImpl deliveryPackage = TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(DeliveryRestDtoMother.randomList())
                .build();

        Async async = context.async();

        xfcdEvents.subscribe(ConfirmableDeliveryEvent.class, new BaseSubscriber<ConfirmableDeliveryEvent>() {
            @Override
            protected void hookOnNext(ConfirmableDeliveryEvent value) {
                async.complete();
            }
        });

        xfcdEvents.publish(IncomingDeliveryEvent.class, Mono.just(IncomingDeliveryEvent.builder()
                .deliveryPackage(deliveryPackage)
                .build()));

        async.await();

        verify(dao, times(1)).save(eq(deliveryPackage));
    }
}