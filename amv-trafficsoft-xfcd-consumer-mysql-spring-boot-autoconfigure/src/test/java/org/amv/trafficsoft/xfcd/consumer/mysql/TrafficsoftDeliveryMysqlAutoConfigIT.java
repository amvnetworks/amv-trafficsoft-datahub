package org.amv.trafficsoft.xfcd.consumer.mysql;

import com.wix.mysql.EmbeddedMysql;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackageImpl;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDtoMother;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryEntity;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcDao;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryPackageJdbcDao;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftXfcdJdbcProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {
        TrafficsoftDeliveryMysqlAutoConfigIT.TestApplictaion.class
})
@TestExecutionListeners({
        DirtiesContextTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class
})
public class TrafficsoftDeliveryMysqlAutoConfigIT {
    @SpringBootApplication
    @Import(EmbeddedDatabaseTestConfig.class)
    public static class TestApplictaion {
        @Bean
        public InitializingBean setJdbcUrlForTests(EmbeddedMysql embeddedMysql,
                                                   TrafficsoftXfcdJdbcProperties properties) {
            final String url = String.format("jdbc:mysql://localhost:%d/%s?" +
                    "profileSQL=true" +
                    "&generateSimpleParameterMetadata=true",
                    embeddedMysql.getConfig().getPort(),
                    EmbeddedDatabaseTestConfig.SCHEMA_NAME);

            return () -> properties.setJdbcUrl(url);
        }
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
