package org.amv.trafficsoft.xfcd.mqtt.moquette.ext;

import java.util.List;
import java.util.Optional;

public interface TopicPolicies {

    List<ITopicPolicy> getPolicies();

    Optional<ITopicPolicy> getFallbackPolicy();
}
