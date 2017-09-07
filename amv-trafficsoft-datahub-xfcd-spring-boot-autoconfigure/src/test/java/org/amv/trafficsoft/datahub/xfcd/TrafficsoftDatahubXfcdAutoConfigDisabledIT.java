package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.core.Vertx;
import org.amv.trafficsoft.rest.client.autoconfigure.TrafficsoftApiRestProperties;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {
        TrafficsoftDatahubXfcdAutoConfigDisabledIT.TestApplictaion.class,
})
@TestPropertySource(locations="classpath:application-it-disabled.properties")
public class TrafficsoftDatahubXfcdAutoConfigDisabledIT {
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
    public void datahubXfcdPropertiesBeanExists() throws Exception {
        final TrafficsoftDatahubXfcdProperties datahubXfcdProperties = applicationContext
                .getBean(TrafficsoftDatahubXfcdProperties.class);

        assertThat(datahubXfcdProperties, is(notNullValue()));
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void xfcdEventsBeanDoesNotExist() throws Exception {
        final XfcdEvents xfcdEvents = applicationContext.getBean(XfcdEvents.class);

        Assert.fail("Should have thrown exception");
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void deliveryRetrievalVerticleBeanDoesNotExist() throws Exception {
        final DeliveryRetrievalVerticle deliveryRetrievalVerticle = applicationContext
                .getBean(DeliveryRetrievalVerticle.class);

        Assert.fail("Should have thrown exception");
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void deliveryConfirmationVerticleBeanDoesNotExist() throws Exception {
        final DeliveryConfirmationVerticle deliveryConfirmationVerticle = applicationContext
                .getBean(DeliveryConfirmationVerticle.class);

        Assert.fail("Should have thrown exception");
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void deliveryDataStoreVerticleBeanDoesNotExist() throws Exception {
        final DeliveryDataStoreVerticle deliveryDataStoreVerticle = applicationContext
                .getBean(DeliveryDataStoreVerticle.class);

        Assert.fail("Should have thrown exception");
    }
}
