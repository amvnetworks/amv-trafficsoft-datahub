package org.amv.spring.vertx;


import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

@Data
@ConfigurationProperties("vertx")
public class VertxProperties {
    /**
     * The default value of warning exception time 5000000000 ns (5 seconds)
     * If a thread is blocked longer than this threshold, the warning log
     * contains a stack trace
     */
    private static final long DEFAULT_WARNING_EXCEPTION_TIME = TimeUnit.SECONDS.toNanos(5);

    // TODO add property class for Metrics, AddressResolverOptions, etc

    private int eventLoopPoolSize = VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE;
    private int workerPoolSize = VertxOptions.DEFAULT_WORKER_POOL_SIZE;
    private int internalBlockingPoolSize = VertxOptions.DEFAULT_INTERNAL_BLOCKING_POOL_SIZE;
    private long blockedThreadCheckInterval = VertxOptions.DEFAULT_BLOCKED_THREAD_CHECK_INTERVAL;
    private long maxEventLoopExecuteTime = VertxOptions.DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME;
    private long maxWorkerExecuteTime = VertxOptions.DEFAULT_MAX_WORKER_EXECUTE_TIME;
    private int quorumSize = VertxOptions.DEFAULT_QUORUM_SIZE;
    private boolean fileResolverCachingEnabled = VertxOptions.DEFAULT_FILE_CACHING_ENABLED;
    private String haGroup = VertxOptions.DEFAULT_HA_GROUP;
    private boolean haEnabled = VertxOptions.DEFAULT_HA_ENABLED;
    private long warningExceptionTime = DEFAULT_WARNING_EXCEPTION_TIME;
    private VertxEventBusProperties eventbus = new VertxEventBusProperties();

    @Data
    public static class VertxEventBusProperties {
        private boolean clustered = VertxOptions.DEFAULT_CLUSTERED;
        private String clusterPublicHost = VertxOptions.DEFAULT_CLUSTER_PUBLIC_HOST;
        private int clusterPublicPort = VertxOptions.DEFAULT_CLUSTER_PUBLIC_PORT;
        private long clusterPingInterval = VertxOptions.DEFAULT_CLUSTER_PING_INTERVAL;
        private long clusterPingReplyInterval = VertxOptions.DEFAULT_CLUSTER_PING_REPLY_INTERVAL;

        private int port = EventBusOptions.DEFAULT_PORT;
        private String host = EventBusOptions.DEFAULT_HOST;
        private int acceptBacklog = EventBusOptions.DEFAULT_ACCEPT_BACKLOG;
        //private ClientAuth clientAuth = DEFAULT_CLIENT_AUTH;

        private int reconnectAttempts = EventBusOptions.DEFAULT_RECONNECT_ATTEMPTS;
        private long reconnectInterval = EventBusOptions.DEFAULT_RECONNECT_INTERVAL;

        private int connectTimeout = EventBusOptions.DEFAULT_CONNECT_TIMEOUT;
        private boolean trustAll = EventBusOptions.DEFAULT_TRUST_ALL;
    }

}
