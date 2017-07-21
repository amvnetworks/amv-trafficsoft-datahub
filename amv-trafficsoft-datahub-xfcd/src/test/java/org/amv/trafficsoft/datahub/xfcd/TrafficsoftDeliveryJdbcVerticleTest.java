package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.core.Vertx;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDtoMother;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.*;

public class TrafficsoftDeliveryJdbcVerticleTest {

    private XfcdDataStore dao;

    private TrafficsoftDeliveryDataStoreVerticle sut;

    @Before
    public void setUp() throws IOException {
        this.dao = spy(XfcdDataStore.class);

        this.sut = TrafficsoftDeliveryDataStoreVerticle.builder()
                .xfcdEvents(new XfcdEvents(Vertx.vertx()))
                .dataStore(this.dao)
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