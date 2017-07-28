package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.core.Vertx;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDtoMother;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.*;

public class TrafficsoftDeliveryJdbcVerticleTest {

    private XfcdDataStore dao;

    private DeliveryDataStoreVerticle sut;

    @Before
    public void setUp() throws IOException {
        this.dao = spy(XfcdDataStore.class);

        this.sut = DeliveryDataStoreVerticle.builder()
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
        
        sut.persistDeliveryPackage(deliveryPackage);
    }

    @Test
    public void itShouldCallSaveActionOnDelivery() throws Exception {
        final TrafficsoftDeliveryPackageImpl deliveryPackage = TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(DeliveryRestDtoMother.randomList())
                .build();

        sut.persistDeliveryPackage(deliveryPackage);

        verify(dao, times(1)).save(eq(deliveryPackage));
    }
}