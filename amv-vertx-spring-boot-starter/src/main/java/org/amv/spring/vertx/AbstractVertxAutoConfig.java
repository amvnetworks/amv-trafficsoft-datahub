package org.amv.spring.vertx;

import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.metrics.MetricsOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public abstract class AbstractVertxAutoConfig {

    private final VertxProperties properties;

    public AbstractVertxAutoConfig(VertxProperties properties) {
        this.properties = requireNonNull(properties);
    }

    @ConditionalOnMissingBean(MetricsOptions.class)
    @Bean
    public MetricsOptions metricsOptions() {
        return new MetricsOptions()
                .setEnabled(false);
    }

    @ConditionalOnMissingBean(EventBusOptions.class)
    @Bean
    public EventBusOptions eventBusOptions() {
        final VertxProperties.VertxEventBusProperties eventBusProperties = Optional.ofNullable(properties.getEventbus())
                .orElse(new VertxProperties.VertxEventBusProperties());

        final EventBusOptions eventBusOptions = new EventBusOptions()
                .setHost(eventBusProperties.getHost())
                .setPort(eventBusProperties.getPort())
                .setClusterPublicHost(eventBusProperties.getClusterPublicHost())
                .setClusterPingInterval(eventBusProperties.getClusterPingInterval())
                .setClusterPingReplyInterval(eventBusProperties.getClusterPingReplyInterval())
                .setAcceptBacklog(eventBusProperties.getAcceptBacklog())
                .setConnectTimeout(eventBusProperties.getConnectTimeout())
                .setReconnectAttempts(eventBusProperties.getReconnectAttempts())
                .setReconnectInterval(eventBusProperties.getReconnectInterval());

        if (eventBusProperties.getClusterPublicPort() != -1) {
            // otherwise IllegalArgumentException is thrown
            eventBusOptions.setClusterPublicPort(eventBusProperties.getClusterPublicPort());
        }

        return eventBusOptions;
    }

    @ConditionalOnMissingBean(VertxOptions.class)
    @Bean
    public VertxOptions vertxOptions(EventBusOptions eventBusOptions, MetricsOptions metricsOptions) {
        return new VertxOptions()
                .setBlockedThreadCheckInterval(properties.getBlockedThreadCheckInterval())
                .setEventLoopPoolSize(properties.getEventLoopPoolSize())
                .setWorkerPoolSize(properties.getWorkerPoolSize())
                .setInternalBlockingPoolSize(properties.getInternalBlockingPoolSize())
                .setQuorumSize(properties.getQuorumSize())
                .setMaxEventLoopExecuteTime(properties.getMaxEventLoopExecuteTime())
                .setHAGroup(properties.getHaGroup())
                .setMaxWorkerExecuteTime(properties.getMaxWorkerExecuteTime())
                .setWarningExceptionTime(properties.getWarningExceptionTime())
                .setFileResolverCachingEnabled(properties.isFileResolverCachingEnabled())
                .setHAEnabled(properties.isHaEnabled())
                .setEventBusOptions(eventBusOptions)
                .setMetricsOptions(metricsOptions);
    }

}
