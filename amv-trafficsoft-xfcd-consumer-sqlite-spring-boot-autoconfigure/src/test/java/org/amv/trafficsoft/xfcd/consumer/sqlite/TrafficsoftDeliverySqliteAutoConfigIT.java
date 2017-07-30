package org.amv.trafficsoft.xfcd.consumer.sqlite;

import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackageImpl;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDtoMother;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryEntity;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcDao;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryPackageJdbcDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {
        TrafficsoftDeliverySqliteAutoConfigIT.TestApplictaion.class
})
public class TrafficsoftDeliverySqliteAutoConfigIT {
    @SpringBootApplication
    public static class TestApplictaion {
    }

    @Autowired
    private TrafficsoftDeliveryPackageJdbcDao deliveryPackageDao;

    @Autowired
    private TrafficsoftDeliveryJdbcDao deliveryDao;

    @Test
    public void itShouldPersistToDatabase() throws Exception {
        final List<DeliveryRestDto> deliveries = DeliveryRestDtoMother.randomList();

        deliveryPackageDao.save(TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(deliveries)
                .build());

        final long deliveryId = deliveries.stream().findFirst()
                .orElseThrow(IllegalStateException::new)
                .getDeliveryId();

        TrafficsoftDeliveryEntity fromDb = this.deliveryDao.findById(deliveryId)
                .orElseThrow(IllegalStateException::new);

        assertThat(fromDb, is(equalTo(fromDb)));
    }


}
