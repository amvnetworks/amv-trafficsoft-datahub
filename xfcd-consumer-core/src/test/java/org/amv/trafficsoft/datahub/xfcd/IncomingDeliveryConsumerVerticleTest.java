package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.core.Vertx;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDtoMother;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.*;

public class IncomingDeliveryConsumerVerticleTest {

    private DeliveryConsumer dao;

    private IncomingDeliveryConsumerVerticle sut;

    @Before
    public void setUp() throws IOException {
        this.dao = spy(DeliveryConsumer.class);

        this.sut = IncomingDeliveryConsumerVerticle.builder()
                .xfcdEvents(new XfcdEvents(Vertx.vertx()))
                .incomingDeliveryEventConsumer(ConfirmingDeliveryConsumer.builder()
                        .deliveryConsumer(this.dao)
                        .confirmDelivery(true)
                        .build())
                .build();
    }

    @Test
    public void itShouldDoNothingOnEmptyList() throws Exception {
        verifyNoMoreInteractions(dao);

        final TrafficsoftDeliveryPackageImpl deliveryPackage = TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(Collections.emptyList())
                .build();

        sut.consumeIncomingDeliveryEvent(IncomingDeliveryEvent.builder()
                .deliveryPackage(deliveryPackage)
                .build());

        verifyZeroInteractions(dao);
    }

    @Test
    public void itShouldCallSaveActionOnDelivery() throws Exception {
        final TrafficsoftDeliveryPackageImpl deliveryPackage = TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(DeliveryRestDtoMother.randomList())
                .build();

        final IncomingDeliveryEvent event = IncomingDeliveryEvent.builder()
                .deliveryPackage(deliveryPackage)
                .build();

        sut.consumeIncomingDeliveryEvent(event);

        verify(dao, times(1)).consume(eq(deliveryPackage));
    }
}
