package org.amv.trafficsoft.xfcd.mqtt.moquette.ext;

import io.moquette.spi.impl.subscriptions.Topic;
import io.moquette.spi.security.IAuthorizator;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.requireNonNull;

@Slf4j
public class SimpleAuthorizator implements IAuthorizator {

    private final TopicPolicies topicPolicies;

    public SimpleAuthorizator(TopicPolicies topicPolicies) {
        this.topicPolicies = requireNonNull(topicPolicies);
    }

    @Override
    public boolean canWrite(Topic topic, String s, String s1) {
        boolean writeEnabledByTopics = topicPolicies.getPolicies().stream()
                .filter(p -> p.supports(topic))
                .anyMatch(ITopicPolicy::isWriteable);

        boolean canWriteByFallback = topicPolicies.getFallbackPolicy()
                .map(ITopicPolicy::isWriteable)
                .orElse(false);

        boolean writeEnabled = writeEnabledByTopics || canWriteByFallback;
        if (!writeEnabled) {
            log.warn("Block WRITE ACCESS for topic {} from {} ({})", topic, s, s1);
        }

        return writeEnabled;
    }

    @Override
    public boolean canRead(Topic topic, String s, String s1) {
        boolean readEnabledByTopics = topicPolicies.getPolicies().stream()
                .filter(p -> p.supports(topic))
                .anyMatch(ITopicPolicy::isReadable);

        boolean canReadByFallback = topicPolicies.getFallbackPolicy()
                .map(ITopicPolicy::isReadable)
                .orElse(false);

        boolean readEnabled = readEnabledByTopics || canReadByFallback;
        if (!readEnabled) {
            log.warn("Block READ ACCESS for topic {} from {} ({})", topic, s, s1);
        }

        return readEnabled;

    }
}
