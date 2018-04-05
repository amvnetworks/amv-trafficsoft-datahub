package org.amv.trafficsoft.xfcd.mqtt.moquette.ext;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Value
@Builder
public class TopicPoliciesImpl implements TopicPolicies {
    @Singular("addPolicy")
    public List<ITopicPolicy> policies;

    public ITopicPolicy fallbackPolicy;

    @Override
    public List<ITopicPolicy> getPolicies() {
        return policies == null ? Collections.emptyList() : ImmutableList.copyOf(policies);
    }

    @Override
    public Optional<ITopicPolicy> getFallbackPolicy() {
        return Optional.ofNullable(fallbackPolicy);
    }
}
