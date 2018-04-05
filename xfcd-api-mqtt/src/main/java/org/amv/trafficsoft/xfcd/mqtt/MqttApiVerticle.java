package org.amv.trafficsoft.xfcd.mqtt;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import io.moquette.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.XfcdEvents;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.BaseSubscriber;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
public class MqttApiVerticle extends AbstractVerticle {
    // todo: should be configurable via properties
    public static final String TOPIC_NAME = "/ro/xfcd/delivery";

    private final String clientId;
    private final Server server;
    private final XfcdEvents xfcdEvents;

    private volatile DeliveryMqttPublisherSubscriber subscriber;

    public MqttApiVerticle(String clientId, Server server, XfcdEvents xfcdEvents) {
        checkArgument(StringUtils.isNotBlank(clientId), "`clientId` must not be blank");

        this.clientId = requireNonNull(clientId);
        this.server = requireNonNull(server);
        this.xfcdEvents = requireNonNull(xfcdEvents);
    }

    @Override
    public synchronized void start() {
        disposeSubscriberIfNecessary();
        this.subscriber = new DeliveryMqttPublisherSubscriber();
        xfcdEvents.subscribe(IncomingDeliveryEvent.class, subscriber);
    }

    @Override
    public synchronized void stop() {
        disposeSubscriberIfNecessary();
    }

    private void disposeSubscriberIfNecessary() {
        if (this.subscriber != null) {
            this.subscriber.dispose();
        }
    }

    private class DeliveryMqttPublisherSubscriber extends BaseSubscriber<IncomingDeliveryEvent> {
        @Override
        protected void hookOnNext(IncomingDeliveryEvent deliveryEvent) {
            MqttPublishMessage mqttMessage = createMqttMessage(deliveryEvent);

            server.internalPublish(mqttMessage, clientId);
        }

        @Override
        protected void hookOnComplete() {
            this.dispose();
        }

        @Override
        protected void hookOnError(Throwable throwable) {
            log.error("", throwable);
            this.dispose();
        }

        private MqttPublishMessage createMqttMessage(IncomingDeliveryEvent event) {
            String contentJson = createContentJson(event);

            ByteBuf playload = Unpooled.copiedBuffer(contentJson.getBytes(Charsets.UTF_8));

            return MqttMessageBuilders.publish()
                    .topicName(TOPIC_NAME)
                    .retained(false)
                    .qos(MqttQoS.AT_LEAST_ONCE)
                    .payload(playload)
                    .build();
        }

        private String createContentJson(IncomingDeliveryEvent event) {
            ImmutableMap<String, Object> content = ImmutableMap.<String, Object>builder()
                    .put("value", event)
                    .build();

            return Json.encode(content);
        }
    }
}