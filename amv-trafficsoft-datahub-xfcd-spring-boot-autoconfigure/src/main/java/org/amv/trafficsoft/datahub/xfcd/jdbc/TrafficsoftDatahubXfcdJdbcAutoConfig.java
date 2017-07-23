package org.amv.trafficsoft.datahub.xfcd.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.DeliveryDataStoreVerticle;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDatahubXfcdAutoConfig;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDatahubXfcdProperties;
import org.amv.trafficsoft.datahub.xfcd.XfcdEvents;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcConsumerAutoConfigCompleted;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryPackageJdbcDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@AutoConfigureAfter({
        TrafficsoftDatahubXfcdAutoConfig.class,
        TrafficsoftDeliveryJdbcConsumerAutoConfigCompleted.class
})
//@AutoConfigureOrder(TrafficsoftDatahubXfcdJdbcAutoConfig.PRIORITY)
@ConditionalOnBean(TrafficsoftDeliveryPackageJdbcDao.class)
public class TrafficsoftDatahubXfcdJdbcAutoConfig {
    /**
     * This is done to run after the auto configuration classes
     * that provide {@link TrafficsoftDeliveryPackageJdbcDao} beans.
     * This is a workaround because these configuration classes are
     * not known in advance an can therefore not be used with @AutoConfigureAfter
     */
    static final int PRIORITY = 10_000;

    private final TrafficsoftDatahubXfcdProperties datahubXfcdProperties;

    @Autowired
    public TrafficsoftDatahubXfcdJdbcAutoConfig(TrafficsoftDatahubXfcdProperties datahubXfcdProperties) {
        this.datahubXfcdProperties = requireNonNull(datahubXfcdProperties);
    }

    @Bean("trafficsoftDeliveryDataStoreJdbcAdapter")
    public XfcdDataStoreJdbc xfcdDataStoreJdbcAdapter(TrafficsoftDeliveryPackageJdbcDao trafficsoftDeliveryPackageJdbcDao) {
        boolean isPrimaryDataStore = "jdbc".equals(datahubXfcdProperties.getPrimaryDataStore());

        return XfcdDataStoreJdbc.builder()
                .deliveryPackageDao(trafficsoftDeliveryPackageJdbcDao)
                .primaryDataStore(isPrimaryDataStore)
                .build();
    }

    @Bean("trafficsoftDeliveryDataStoreJdbcVerticle")
    public DeliveryDataStoreVerticle trafficsoftDeliveryDataStoreVerticle(XfcdEvents xfcdEvents,
                                                                          XfcdDataStoreJdbc dataStoreJdbcAdapter) {
        return DeliveryDataStoreVerticle.builder()
                .xfcdEvents(xfcdEvents)
                .dataStore(dataStoreJdbcAdapter)
                .build();
    }

}
