package org.amv.trafficsoft.datahub.xfcd;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("amv.trafficsoft.datahub.xfcd")
public class TrafficsoftDatahubXfcdProperties {

    private boolean enabled;
}
