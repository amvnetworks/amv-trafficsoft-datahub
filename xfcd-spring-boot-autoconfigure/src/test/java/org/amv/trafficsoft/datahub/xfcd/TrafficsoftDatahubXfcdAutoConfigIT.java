package org.amv.trafficsoft.datahub.xfcd;

import io.prometheus.client.CollectorRegistry;
import io.vertx.core.Vertx;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import org.amv.trafficsoft.rest.client.autoconfigure.TrafficsoftApiRestProperties;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDtoMother;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.util.concurrent.CountDownLatch;

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

    /**
     * In case the xfcd datahub module is disabled (e.g. during development)
     * a bean of XfcdEvents must be in the context for the application
     * to start up normally as other implementations may depend on it.
     * (Even if no data is published.)
     */
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

    @Test
    public void itShouldCollectBasicMetrics() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final XfcdEvents xfcdEvents = applicationContext.getBean(XfcdEvents.class);

        assertThat(xfcdEvents, is(notNullValue()));

        xfcdEvents.subscribe(IncomingDeliveryEvent.class, new BaseSubscriber<IncomingDeliveryEvent>() {
            @Override
            protected void hookOnNext(IncomingDeliveryEvent event) {
                Vertx.vertx().setTimer(1, foo -> latch.countDown());
            }
        });

        xfcdEvents.publish(IncomingDeliveryEvent.class, Flux.just(IncomingDeliveryEvent.builder()
                .deliveryPackage(TrafficsoftDeliveryPackageImpl.builder()
                        .deliveries(DeliveryRestDtoMother.randomList())
                        .build())
                .build()));

        latch.await();

        final Double incomingDeliveryCount = CollectorRegistry.defaultRegistry
                .getSampleValue("datahub_incoming_delivery_count");

        assertThat(incomingDeliveryCount, is(1d));

        final Double confirmedDeliveryCount = CollectorRegistry.defaultRegistry
                .getSampleValue("datahub_confirmed_delivery_count");

        assertThat(confirmedDeliveryCount, is(0d));
    }
}
