package org.amv.trafficsoft.datahub.xfcd;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

@Data
@ConfigurationProperties("amv.trafficsoft.datahub.xfcd")
public class TrafficsoftDatahubXfcdProperties {

    private boolean enabled;
    private long fetchIntervalInSeconds = TimeUnit.MINUTES.toSeconds(1);
}