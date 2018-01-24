package org.amv.trafficsoft.datahub.xfcd;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

@Data
@ConfigurationProperties("amv.trafficsoft.datahub.xfcd")
public class TrafficsoftDatahubXfcdProperties {
    private static final long MIN_INTERVAL_IN_SECONDS = 30L;

    private boolean enabled;
    private long initialFetchDelayInSeconds = TimeUnit.SECONDS.toSeconds(10);
    private long fetchIntervalInSeconds = TimeUnit.MINUTES.toSeconds(1);
    private int maxAmountOfNodesPerDelivery = 5_000;
    private boolean refetchImmediatelyOnDeliveryWithMaxAmountOfNodes = true;

    public long getFetchIntervalInSeconds() {
        return Math.max(fetchIntervalInSeconds, MIN_INTERVAL_IN_SECONDS);
    }
    public long getInitialFetchDelayInSeconds() {
        return Math.max(initialFetchDelayInSeconds, 1);
    }

}
