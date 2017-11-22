package org.amv.trafficsoft.datahub.xfcd.demo;

import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDatahubXfcdAutoConfig;
import org.amv.trafficsoft.datahub.xfcd.XfcdEvents;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@AutoConfigureAfter(TrafficsoftDatahubXfcdAutoConfig.class)
@ConditionalOnProperty(value = "demo.enabled", havingValue = "true")
public class DemoConfig {

    @Bean
    public DemoDeliveryProducerVerticle demoDeliveryProducerVerticle(XfcdEvents xfcdEvents) {
        return new DemoDeliveryProducerVerticle(xfcdEvents);
    }
}
