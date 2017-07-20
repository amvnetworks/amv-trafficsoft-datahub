package org.amv.trafficsoft.datahub.xfcd.jdbc;

import io.vertx.core.Vertx;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackageImpl;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import org.amv.trafficsoft.datahub.xfcd.event.XfcdEvents;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDtoMother;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryPackageJdbcDao;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.*;

public class TrafficsoftDeliveryJdbcVerticleTest {

    private TrafficsoftDeliveryPackageJdbcDao dao;

    private TrafficsoftDeliveryJdbcVerticle sut;

    @Before
    public void setUp() throws IOException {
        this.dao = spy(TrafficsoftDeliveryPackageJdbcDao.class);

        this.sut = TrafficsoftDeliveryJdbcVerticle.builder()
                .xfcdEvents(new XfcdEvents(Vertx.vertx()))
                .deliveryPackageDao(this.dao)
                .build();
    }

    @Test
    public void itShouldDoNothingOnEmptyList() throws Exception {
        verifyNoMoreInteractions(dao);

        final TrafficsoftDeliveryPackageImpl deliveryPackage = TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(Collections.emptyList())
                .build();

        final IncomingDeliveryEvent incomingDeliveryEvent = IncomingDeliveryEvent.builder()
                .deliveryPackage(deliveryPackage)
                .build();

        sut.onIncomingDeliveryPackage(incomingDeliveryEvent);
    }

    @Test
    public void itShouldCallSaveActionOnDelivery() throws Exception {
        final TrafficsoftDeliveryPackageImpl deliveryPackage = TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(DeliveryRestDtoMother.randomList())
                .build();

        final IncomingDeliveryEvent incomingDeliveryEvent = IncomingDeliveryEvent.builder()
                .deliveryPackage(deliveryPackage)
                .build();

        sut.onIncomingDeliveryPackage(incomingDeliveryEvent);

        verify(dao, times(1)).save(eq(deliveryPackage));
    }
}