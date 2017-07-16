package org.amv.trafficsoft.xfcd.consumer.sqlite;

import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackageSubscriber;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackageSubscriberEventBusAdapter;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcConsumerAutoConfig;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcDao;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcPackageSubscriberImpl;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryRowMapper;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.function.Supplier;

@Slf4j
@Configuration
@AutoConfigureAfter(TrafficsoftDeliveryJdbcConsumerAutoConfig.class)
@ConditionalOnClass(org.sqlite.JDBC.class)
@ConditionalOnProperty(
        value = "amv.trafficsoft.xfcd.consumer.jdbc.driverClassName",
        havingValue = "org.sqlite.JDBC"
)
@ConditionalOnBean(name = "trafficsoftDeliveryJdbcConsumerNamedTemplate")
@EnableTransactionManagement
public class TrafficsoftDeliverySqliteConsumerAutoConfig {

    @Autowired
    @Qualifier("trafficsoftDeliveryJdbcConsumerNamedTemplate")
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    @ConditionalOnBean(EventBus.class)
    @Bean("sqliteTrafficsoftDeliverySubscriberEventBusAdapter")
    public TrafficsoftDeliveryPackageSubscriberEventBusAdapter deliverySubscriberEventBusAdapter(TrafficsoftDeliveryJdbcDao deliveryDao, EventBus eventBus) {
        final Supplier<Subscriber<TrafficsoftDeliveryPackage>> trafficsoftDeliverySubscriberSupplier = () -> {
            return new TrafficsoftDeliveryJdbcPackageSubscriberImpl(deliveryDao);
        };
        return new TrafficsoftDeliveryPackageSubscriberEventBusAdapter(trafficsoftDeliverySubscriberSupplier, eventBus);
    }

    @ConditionalOnMissingBean
    @Bean("trafficsoftDeliveryRowMapper")
    public TrafficsoftDeliveryRowMapper deliveryRowMapper() {
        return new TrafficsoftDeliveryRowMapper();
    }

    @Bean("sqliteTrafficsoftDeliveryJdbcDao")
    public TrafficsoftDeliveryJdbcDao deliveryDao(TrafficsoftDeliveryRowMapper deliveryRowMapper) {
        return new SqliteTrafficsoftDeliveryJdbcDao(namedJdbcTemplate, deliveryRowMapper);
    }
}
