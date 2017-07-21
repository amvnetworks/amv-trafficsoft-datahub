package org.amv.trafficsoft.datahub.xfcd.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDatahubXfcdAutoConfig;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDatahubXfcdProperties;
import org.amv.trafficsoft.datahub.xfcd.DeliveryDataStoreVerticle;
import org.amv.trafficsoft.datahub.xfcd.XfcdEvents;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcConsumerAutoConfig;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryPackageJdbcDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@AutoConfigureAfter({
        TrafficsoftDatahubXfcdAutoConfig.class,
        TrafficsoftDeliveryJdbcConsumerAutoConfig.class
})
public class TrafficsoftDatahubXfcdJdbcAutoConfig {


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
