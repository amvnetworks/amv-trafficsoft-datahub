package org.amv.trafficsoft.xfcd.consumer.sqlite;

import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackageImpl;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDtoMother;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryEntity;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {
        TrafficsoftDeliverySqliteConsumerIT.TestApplictaion.class
})
public class TrafficsoftDeliverySqliteConsumerIT {
    @SpringBootApplication
    public static class TestApplictaion {

    }

    @Autowired
    private TrafficsoftDeliveryJdbcDao deliveryDao;

    @Autowired
    private TrafficsoftDeliverySqliteConsumer sut;

    @Test
    public void itShouldPersistToDatabase() throws Exception {
        final List<DeliveryRestDto> deliveryRestDto = DeliveryRestDtoMother.randomList();

        Flux.fromIterable(deliveryRestDto)
                .map(delivery -> TrafficsoftDeliveryPackageImpl.builder()
                        .addDelivery(delivery)
                        .build())
                .subscribe(sut);

        final long deliveryId = deliveryRestDto.stream().findFirst()
                .orElseThrow(IllegalStateException::new)
                .getDeliveryId();

        TrafficsoftDeliveryEntity fromDb = this.deliveryDao.findById(deliveryId)
                .orElseThrow(IllegalStateException::new);

        assertThat(fromDb, is(equalTo(fromDb)));
    }

}
