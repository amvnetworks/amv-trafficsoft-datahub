package org.amv.trafficsoft.xfcd.mqtt.moquette.handler;

import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoopHandler implements InterceptHandler {
    private static final Class[] EMPTY_CLASS_ARRAY = {};

    @Override
    public String getID() {
        return "NoopHandler";
    }

    @Override
    public Class<?>[] getInterceptedMessageTypes() {
        return EMPTY_CLASS_ARRAY;
    }

    @Override
    public void onConnect(InterceptConnectMessage msg) {

    }

    @Override
    public void onDisconnect(InterceptDisconnectMessage msg) {

    }

    @Override
    public void onConnectionLost(InterceptConnectionLostMessage msg) {

    }

    @Override
    public void onPublish(InterceptPublishMessage msg) {

    }

    @Override
    public void onSubscribe(InterceptSubscribeMessage msg) {

    }

    @Override
    public void onUnsubscribe(InterceptUnsubscribeMessage msg) {

    }

    @Override
    public void onMessageAcknowledged(InterceptAcknowledgedMessage msg) {

    }
}