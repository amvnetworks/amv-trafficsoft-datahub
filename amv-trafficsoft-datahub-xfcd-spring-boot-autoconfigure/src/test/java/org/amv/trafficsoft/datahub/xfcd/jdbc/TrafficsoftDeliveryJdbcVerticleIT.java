package org.amv.trafficsoft.datahub.xfcd.jdbc;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackageImpl;
import org.amv.trafficsoft.datahub.xfcd.event.VertxEvents;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDtoMother;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(VertxUnitRunner.class)
public class TrafficsoftDeliveryJdbcVerticleIT {

    private Vertx vertx;

    private TrafficsoftDeliveryJdbcDao jdbcDao;

    @Before
    public void setUp(TestContext context) throws IOException {
        this.vertx = Vertx.vertx();
        this.jdbcDao = spy(TrafficsoftDeliveryJdbcDao.class);

        final TrafficsoftDeliveryJdbcVerticle sut = TrafficsoftDeliveryJdbcVerticle.builder()
                .deliveryDao(this.jdbcDao)
                .primaryDataStore(true)
                .build();

        vertx.deployVerticle(sut, context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }


    @Test
    public void itShouldDoNothingOnEmptyList(TestContext context) throws Exception {
        verifyNoMoreInteractions(jdbcDao);

        final TrafficsoftDeliveryPackage deliveryPackage = TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(Collections.emptyList())
                .build();

        Async async = context.async();
        vertx.eventBus().consumer(VertxEvents.deliveryPackage, msg -> {
            async.complete();
        });

        vertx.eventBus().publish(VertxEvents.deliveryPackage, Json.encode(deliveryPackage));

        async.await();

    }

    @Test
    public void itShouldCallSaveActionOnDelivery(TestContext context) throws Exception {
        final TrafficsoftDeliveryPackageImpl deliveryPackage = TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(DeliveryRestDtoMother.randomList())
                .build();

        Async async = context.async();
        vertx.eventBus().consumer(VertxEvents.deliveryPackageInternallyConfirmed, msg -> {
            async.complete();
        });

        vertx.eventBus().publish(VertxEvents.deliveryPackage, Json.encode(deliveryPackage));

        async.await();

        verify(jdbcDao, times(1)).saveAll(anyList());
    }
}