package org.amv.trafficsoft.datahub.example;

import io.vertx.core.Vertx;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.XfcdEvents;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryEntity;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcDao;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@TestConfiguration
public class TestConfig {

    @Bean
    public Vertx vertx() {
        return Vertx.vertx();
    }

    @Bean
    public XfcdEvents xfcdEvents(Vertx vertx) {
        return new XfcdEvents(vertx);
    }

    @Bean
    public TrafficsoftDeliveryJdbcDao trafficsoftDeliveryJdbcDaoStub() {
        return new TrafficsoftDeliveryJdbcDao() {
            @Override
            public void saveAll(List<TrafficsoftDeliveryEntity> deliveries) {
                // no-op for tests
            }

            @Override
            public List<TrafficsoftDeliveryEntity> findByIds(List<Long> deliveryIds) {
                return Collections.emptyList();
            }

            @Override
            public List<Long> findIdsOfUnconfirmedDeliveriesByBpcId(long bpcId) {
                return Collections.emptyList();
            }

            @Override
            public void confirmDeliveriesByIds(Collection<Long> ids) {
                // no-op for tests
            }
        };
    }

    @Bean
    public org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryPackageJdbcDao trafficsoftDeliveryPackageJdbcDaoStub() {
        // Provide a minimal no-op implementation to satisfy auto-config dependencies during tests
        return new org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryPackageJdbcDao() {
            @Override
            public void save(org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage deliveryPackage) {
                // no-op
            }
        };
    }
}
