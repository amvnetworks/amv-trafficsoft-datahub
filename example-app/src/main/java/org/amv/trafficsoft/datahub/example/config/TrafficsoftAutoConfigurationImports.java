package org.amv.trafficsoft.datahub.example.config;

import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDatahubXfcdAutoConfig;
import org.amv.trafficsoft.rest.client.autoconfigure.TrafficsoftApiRestClientAutoConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Explicitly imports the AMV Trafficsoft REST client and Datahub XFCD auto-configurations.
 *
 * Why: In some environments the starters' auto-configs may not be picked up automatically
 * under Spring Boot 3.x. Importing them conditionally ensures the REST client and XFCD
 * infrastructure are created when XFCD is enabled.
 */
@Configuration
@ConditionalOnProperty(prefix = "amv.trafficsoft.datahub.xfcd", name = "enabled", havingValue = "true", matchIfMissing = false)
@Import({
        TrafficsoftApiRestClientAutoConfig.class,
        TrafficsoftDatahubXfcdAutoConfig.class
})
public class TrafficsoftAutoConfigurationImports {
}
