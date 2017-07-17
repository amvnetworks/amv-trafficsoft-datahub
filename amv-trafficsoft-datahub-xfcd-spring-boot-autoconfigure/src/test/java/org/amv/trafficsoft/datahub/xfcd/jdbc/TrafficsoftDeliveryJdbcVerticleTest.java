package org.amv.trafficsoft.datahub.xfcd.jdbc;

import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackageImpl;
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
                .deliveryPackageDao(this.dao)
                .build();
    }


    @Test
    public void itShouldDoNothingOnEmptyList() throws Exception {
        final TrafficsoftDeliveryPackageImpl deliveryPackage = TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(Collections.emptyList())
                .build();

        sut.onNext(deliveryPackage);

        verify(dao, never());
    }

    @Test
    public void itShouldCallSaveActionOnDelivery() throws Exception {
        final TrafficsoftDeliveryPackageImpl deliveryPackage = TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(DeliveryRestDtoMother.randomList())
                .build();

        sut.onNext(deliveryPackage);

        verify(dao, times(1)).save(eq(deliveryPackage));
    }
}