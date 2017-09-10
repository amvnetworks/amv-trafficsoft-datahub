package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.core.Vertx;
import org.amv.trafficsoft.rest.client.autoconfigure.TrafficsoftApiRestProperties;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
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
        TrafficsoftDatahubXfcdAutoConfigIT.TestApplictaion.class,
})
public class TrafficsoftDatahubXfcdAutoConfigIT {
    @SpringBootApplication
    @Import(TrafficsoftDatahubXfcdAutoConfig.class)
    public static class TestApplictaion {
        @Bean
        public Vertx vertx() {
            return Vertx.vertx();
        }

        @Bean
        public TrafficsoftApiRestProperties apiRestProperties() {
            return new TrafficsoftApiRestProperties();
        }

        @Bean
        public XfcdClient xfcdClient() {
            return mock(XfcdClient.class);
        }
    }

    @Autowired
    ApplicationContext applicationContext;

    @Test
    public void contextLoads() throws Exception {

    }

    @Test
    public void xfcdEventsBeanExists() throws Exception {
        final XfcdEvents xfcdEvents = applicationContext.getBean(XfcdEvents.class);

        assertThat(xfcdEvents, is(notNullValue()));
    }

    @Test
    public void datahubXfcdPropertiesBeanExists() throws Exception {
        final TrafficsoftDatahubXfcdProperties datahubXfcdProperties = applicationContext
                .getBean(TrafficsoftDatahubXfcdProperties.class);

        assertThat(datahubXfcdProperties, is(notNullValue()));
    }

    @Test
    public void deliveryRetrievalVerticleBeanExists() throws Exception {
        final DeliveryRetrievalVerticle deliveryRetrievalVerticle = applicationContext
                .getBean(DeliveryRetrievalVerticle.class);

        assertThat(deliveryRetrievalVerticle, is(notNullValue()));
    }

    @Test
    public void deliveryConfirmationVerticleBeanExists() throws Exception {
        final DeliveryConfirmationVerticle deliveryConfirmationVerticle = applicationContext
                .getBean(DeliveryConfirmationVerticle.class);

        assertThat(deliveryConfirmationVerticle, is(notNullValue()));
    }
}
