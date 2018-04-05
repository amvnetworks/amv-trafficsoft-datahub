package org.amv.trafficsoft.xfcd.mqtt.moquette.ext;

import io.moquette.spi.impl.subscriptions.Topic;

public interface ITopicPolicy {

    boolean supports(Topic topicName);

    boolean isReadable();

    boolean isWriteable();
}
