package org.amv.trafficsoft.restclient.demo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "customer")
public class CustomerProperties {
    private String baseUrl;
    private String username;
    private String password;
    private long contractId;
    private List<Long> vehicleIds;
}