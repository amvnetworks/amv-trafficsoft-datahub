package org.amv.trafficsoft.xfcd.consumer.sqlite;

import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackageImpl;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.rest.xfcd.model.TrackRestDto;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcDao;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.sql.Date;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class TrafficsoftDeliverySqliteConsumerTest {

    private TrafficsoftDeliverySqliteConsumer sut;

    private TrafficsoftDeliveryJdbcDao dao;

    @Before
    public void setUp() {
        this.dao = spy(TrafficsoftDeliveryJdbcDao.class);

        this.sut = spy(new TrafficsoftDeliverySqliteConsumer(dao));
    }

    @Test(expected = NullPointerException.class)
    public void itShouldThrowOnNull() throws Exception {
        sut.onNext(null);

        Assert.fail("It should have thrown exception");
    }

    @Test
    public void itShouldDoNothingOnEmptyList() throws Exception {
        List<DeliveryRestDto> deliveryRestDtoList = Collections.emptyList();

        sut.onNext(TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(deliveryRestDtoList)
                .build());

        verify(dao, never());
    }

    @Test
    public void itShouldCallSaveActionOnDelivery() throws Exception {
        List<DeliveryRestDto> deliveryRestDtoList = Collections.singletonList(DeliveryRestDto.builder()
                .deliveryId(RandomUtils.nextLong())
                .timestamp(Date.from(Instant.now()))
                .addTrack(TrackRestDto.builder()
                        .id(RandomUtils.nextLong())
                        .vehicleId(RandomUtils.nextLong())
                        .build())
                .build());

        Flux.fromIterable(deliveryRestDtoList)
                .map(delivery -> TrafficsoftDeliveryPackageImpl.builder()
                        .addDelivery(delivery)
                        .build())
                .subscribeOn(Schedulers.immediate())
                .subscribe(sut);

        verify(dao, times(1)).saveAll(anyList());
    }

}
