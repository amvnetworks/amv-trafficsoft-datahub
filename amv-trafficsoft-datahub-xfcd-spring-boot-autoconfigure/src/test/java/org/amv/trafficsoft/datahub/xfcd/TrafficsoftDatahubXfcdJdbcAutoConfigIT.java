package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.core.Vertx;
import org.amv.trafficsoft.datahub.xfcd.jdbc.XfcdDataStoreJdbc;
import org.amv.trafficsoft.rest.client.autoconfigure.TrafficsoftApiRestProperties;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryPackageJdbcDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {
        TrafficsoftDatahubXfcdJdbcAutoConfigIT.TestApplictaion.class,
})
public class TrafficsoftDatahubXfcdJdbcAutoConfigIT {
    @SpringBootApplication
    @Import(TrafficsoftDatahubXfcdAutoConfig.class)
    public static class TestApplictaion {
        @Bean
        public Vertx vertx() {
            return Vertx.vertx();
        }

        /**
         * This bean simulates an inclusion of an
         * "consumer-jdbc-${technology}-autoconfigure module.
         * JdbcAutoConfig is triggered by availability of this bean.
         */
        @Bean
        public TrafficsoftDeliveryPackageJdbcDao deliveryPackageJdbcDao() {
            return mock(TrafficsoftDeliveryPackageJdbcDao.class);
        }

        @Bean
        public TrafficsoftApiRestProperties apiRestProperties() {
            return new TrafficsoftApiRestProperties();
        }

        @Bean
        public XfcdClient xfcdClient() {
            return mock(XfcdClient.class);
        }


        @Bean
        public TrafficsoftDatahubXfcdProperties datahubXfcdProperties() {
            final TrafficsoftDatahubXfcdProperties properties = new TrafficsoftDatahubXfcdProperties();
            properties.setEnabled(true);
            return properties;
        }
    }

    @Autowired
    ApplicationContext applicationContext;

    @Test
    public void contextLoads() throws Exception {
        final XfcdDataStoreJdbc dataStoreJdbc = applicationContext.getBean(XfcdDataStoreJdbc.class);
        final XfcdEvents xfcdEvents = applicationContext.getBean(XfcdEvents.class);

        assertThat(dataStoreJdbc, is(notNullValue()));
        assertThat(xfcdEvents, is(notNullValue()));
    }
}
