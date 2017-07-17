package org.amv.trafficsoft.xfcd.consumer.sqlite;

import com.google.common.eventbus.EventBus;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDtoMother;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryEntity;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static java.util.stream.Collectors.toList;
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
        @Bean
        public EventBus eventBus() {
            return new EventBus();
        }
    }

    @Autowired
    private TrafficsoftDeliveryJdbcDao deliveryDao;

    @Test
    public void itShouldPersistToDatabase() throws Exception {
        final List<DeliveryRestDto> deliveries = DeliveryRestDtoMother.randomList();

        final List<TrafficsoftDeliveryEntity> deliveryEntities = deliveries.stream()
                .map(val -> TrafficsoftDeliveryEntity.builder()
                        .id(val.getDeliveryId())
                        .timestamp(val.getTimestamp().toInstant())
                        .confirmedAt(null)
                        .build())
                .collect(toList());

        deliveryDao.saveAll(deliveryEntities);

        final long deliveryId = deliveries.stream().findFirst()
                .orElseThrow(IllegalStateException::new)
                .getDeliveryId();

        TrafficsoftDeliveryEntity fromDb = this.deliveryDao.findById(deliveryId)
                .orElseThrow(IllegalStateException::new);

        assertThat(fromDb, is(equalTo(fromDb)));
    }

}
