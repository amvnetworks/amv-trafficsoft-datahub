package org.amv.trafficsoft.datahub.xfcd;

@FunctionalInterface
public interface Confirmation {
    static Confirmation doNothing() {
        return (consumer) -> {
        };
    }

    void confirm(IncomingDeliveryEventConsumer consumer);
}
