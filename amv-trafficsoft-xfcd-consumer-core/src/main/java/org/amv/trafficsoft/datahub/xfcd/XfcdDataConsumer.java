package org.amv.trafficsoft.datahub.xfcd;

/**
 * An interface representing a consumer of {@link TrafficsoftDeliveryPackage}
 * from the AMV TrafficSoft xfcd API.
 * <p>
 * A data store can be marked as "primary" which means that
 * the AMV TrafficSoft API can be notified about successfully saved
 * {@link TrafficsoftDeliveryPackage} objects.
 */
public interface XfcdDataConsumer {
    void save(TrafficsoftDeliveryPackage deliveryPackage);

    /**
     * @return true if the store will confirm saved deliveries
     */
    boolean sendsConfirmationEvents();
}
