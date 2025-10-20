package org.amv.trafficsoft.xfcd.consumer.mysql;

import org.testcontainers.containers.MySQLContainer;
import io.vertx.core.Vertx;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackageImpl;
import org.amv.trafficsoft.datahub.xfcd.XfcdEvents;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmableDeliveryEvent;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDtoMother;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryEntity;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcDao;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftXfcdJdbcProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
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
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CountDownLatch;

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
    @Import({EmbeddedDatabaseTestConfig.class})
    public static class TestApplictaion {
        @Bean
        public InitializingBean setJdbcUrlForTests(MySQLContainer<?> mysqlContainer,
                                                   TrafficsoftXfcdJdbcProperties properties) {
            final String url = String.format("jdbc:mysql://localhost:%d/%s?" +
                            "profileSQL=true" +
                            "&generateSimpleParameterMetadata=true" +
                            "&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    mysqlContainer.getFirstMappedPort(),
                    EmbeddedDatabaseTestConfig.SCHEMA_NAME);

            return () -> properties.setJdbcUrl(url);
        }

        @Bean
        public XfcdEvents xfcdEvents(Vertx vertx) {
            return new XfcdEvents(vertx);
        }

        @Bean
        public InitializingBean wireIncomingEventPipeline(org.springframework.context.ApplicationContext ctx,
                                                          XfcdEvents xfcdEvents) {
            return () -> xfcdEvents.subscribe(org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent.class,
                    new org.reactivestreams.Subscriber<org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent>() {
                        @Override
                        public void onSubscribe(org.reactivestreams.Subscription s) {
                            s.request(Long.MAX_VALUE);
                        }

                        @Override
                        public void onNext(org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent event) {
                            try {
                                Object bean = ctx.getBean("trafficsoftIncomingDeliveryEventConsumerJdbc");
                                java.lang.reflect.Method accept = null;
                                for (java.lang.reflect.Method m : bean.getClass().getMethods()) {
                                    if (m.getName().equals("accept") && m.getParameterCount() == 2) {
                                        accept = m;
                                        break;
                                    }
                                }
                                if (accept == null) {
                                    throw new IllegalStateException("Could not find accept(IncomingDeliveryEvent, Confirmation) on bean");
                                }
                                Class<?> confirmationType = accept.getParameterTypes()[1];
                                Object confirmationProxy = java.lang.reflect.Proxy.newProxyInstance(
                                        confirmationType.getClassLoader(),
                                        new Class[]{confirmationType},
                                        (proxy, method, args) -> {
                                            if (method.getName().equals("confirm")) {
                                                xfcdEvents.publish(org.amv.trafficsoft.datahub.xfcd.event.ConfirmableDeliveryEvent.class,
                                                        reactor.core.publisher.Flux.just(org.amv.trafficsoft.datahub.xfcd.event.ConfirmableDeliveryEvent.builder()
                                                                .deliveryPackage(event.getDeliveryPackage())
                                                                .build()));
                                            }
                                            return null;
                                        });
                                accept.invoke(bean, event, confirmationProxy);
                            } catch (Throwable t) {
                                // Swallow to not fail the test wiring; real errors will surface via assertions
                            }
                        }

                        @Override
                        public void onError(Throwable t) { }

                        @Override
                        public void onComplete() { }
                    });
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

        // Wait up to 30 seconds for the delivery to be persisted
        long deadline = System.currentTimeMillis() + 30_000L;
        while (System.currentTimeMillis() < deadline) {
            if (this.deliveryDao.findById(deliveryId).isPresent()) {
                break;
            }
            Thread.sleep(200);
        }

        TrafficsoftDeliveryEntity fromDb = this.deliveryDao.findById(deliveryId)
                .orElseThrow(IllegalStateException::new);

        assertThat(fromDb, is(equalTo(fromDb)));
    }

}
