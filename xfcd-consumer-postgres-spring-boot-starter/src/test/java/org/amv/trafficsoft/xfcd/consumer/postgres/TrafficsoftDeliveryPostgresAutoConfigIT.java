package org.amv.trafficsoft.xfcd.consumer.postgres;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import io.vertx.core.Vertx;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackageImpl;
import org.amv.trafficsoft.datahub.xfcd.XfcdEvents;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmableDeliveryEvent;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDtoMother;
import org.amv.trafficsoft.xfcd.consumer.jdbc.JdbcIncomingDeliveryConsumerAutoConfig;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryEntity;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcDao;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftXfcdJdbcProperties;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.amv.trafficsoft.xfcd.consumer.jdbc.JdbcIncomingDeliveryConsumerAutoConfig.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {
        TrafficsoftDeliveryPostgresAutoConfigIT.TestApplictaion.class
})
@TestExecutionListeners({
        DirtiesContextTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class
})
public class TrafficsoftDeliveryPostgresAutoConfigIT {

    @SpringBootApplication
    @Import(EmbeddedPostgresConfig.class)
    public static class TestApplictaion {
        @Bean
        public InitializingBean setJdbcUrlForTests(EmbeddedPostgres embeddedPostgres,
                                                   TrafficsoftXfcdJdbcProperties properties) {
            final String url = embeddedPostgres.getJdbcUrl("postgres", "postgres");
            properties.setJdbcUrl(url);
            return () -> {};
        }

        @Bean
        public XfcdEvents xfcdEvents(Vertx vertx) {
            return new XfcdEvents(vertx);
        }
    }

    @Autowired
    private TrafficsoftXfcdJdbcProperties properties;

    @Autowired
    private TrafficsoftDeliveryJdbcDao deliveryDao;

    @Autowired
    private XfcdEvents xfcdEvents;

    @Test
    public void itShouldPersistToDatabase() throws Exception {
        assertThat(properties.isSendConfirmationEvents(), is(true));

        CountDownLatch latch = new CountDownLatch(1);

        List<DeliveryRestDto> deliveries = DeliveryRestDtoMother.randomList();

        long deliveryId = deliveries.stream().findFirst()
                .orElseThrow(IllegalStateException::new)
                .getDeliveryId();

        TrafficsoftDeliveryPackage deliveryPackage = TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(deliveries)
                .build();

        xfcdEvents.publish(IncomingDeliveryEvent.class, Mono.just(IncomingDeliveryEvent.builder()
                .deliveryPackage(deliveryPackage)
                .build()));

        xfcdEvents.subscribe(ConfirmableDeliveryEvent.class, new Subscriber<ConfirmableDeliveryEvent>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }

            @Override
            public void onNext(ConfirmableDeliveryEvent event) {
                assertThat(event.getDeliveryPackage(), equalTo(deliveryPackage));
                latch.countDown();
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        latch.await();

        TrafficsoftDeliveryEntity fromDb = this.deliveryDao.findById(deliveryId)
                .orElseThrow(IllegalStateException::new);

        assertThat(fromDb, is(equalTo(fromDb)));
    }

}
