package org.amv.trafficsoft.datahub.xfcd;

/**
 * An interface representing a consumer of {@link TrafficsoftDeliveryPackage}
 * from the AMV TrafficSoft xfcd API.
 */
public interface DeliveryConsumer {
    void consume(TrafficsoftDeliveryPackage deliveryPackage);
}
