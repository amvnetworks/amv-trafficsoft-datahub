package org.amv.trafficsoft.restclient.demo;

import com.netflix.hystrix.*;
import feign.Feign;
import feign.hystrix.SetterFactory;
import org.amv.trafficsoft.rest.client.ClientConfig;
import org.amv.trafficsoft.rest.client.ClientConfig.ConfigurableClientConfig;
import org.amv.trafficsoft.rest.client.TrafficsoftClients;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

@Configuration
@EnableConfigurationProperties(CustomerProperties.class)
public class TrafficsoftRestClientConfig {

    private final CustomerProperties customerProperties;

    @Autowired
    public TrafficsoftRestClientConfig(CustomerProperties customerProperties) {
        this.customerProperties = requireNonNull(customerProperties);
    }

    @Bean
    public XfcdClient xfcdClient() {
        return TrafficsoftClients.xfcd(xfcdClientConfig());
    }

    @Bean
    public ClientConfig.BasicAuth basicAuth() {
        return ClientConfig.BasicAuthImpl.builder()
                .username(customerProperties.getUsername())
                .password(customerProperties.getPassword())
                .build();
    }

    @Bean
    public ConfigurableClientConfig<XfcdClient> xfcdClientConfig() {
        return TrafficsoftClients.config(XfcdClient.class, this.customerProperties.getBaseUrl(), basicAuth())
                .setterFactory(setterFactory())
                .build();
    }

    @Bean
    public SetterFactory setterFactory() {
        return (target, method) -> {
            String groupKey = target.name();
            String commandKey = Feign.configKey(target.type(), method);

            HystrixThreadPoolProperties.Setter threadPoolProperties = HystrixThreadPoolProperties.Setter()
                    .withCoreSize(1);

            HystrixCommandProperties.Setter commandProperties = HystrixCommandProperties.Setter()
                    .withRequestLogEnabled(true)
                    .withFallbackEnabled(false)
                    .withExecutionTimeoutEnabled(true)
                    .withExecutionTimeoutInMilliseconds((int) SECONDS.toMillis(45))
                    .withExecutionIsolationStrategy(SEMAPHORE)
                    .withExecutionIsolationSemaphoreMaxConcurrentRequests(20);

            return HystrixCommand.Setter
                    .withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
                    .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey))
                    .andThreadPoolPropertiesDefaults(threadPoolProperties)
                    .andCommandPropertiesDefaults(commandProperties);
        };
    }

}
