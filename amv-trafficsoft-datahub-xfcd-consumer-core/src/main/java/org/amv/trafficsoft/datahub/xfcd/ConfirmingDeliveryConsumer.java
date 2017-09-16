package org.amv.trafficsoft.datahub.xfcd;

import lombok.Builder;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;

import static java.util.Objects.requireNonNull;

/**
 * A class that decorates a {@link DeliveryConsumer} with an optional
 * choice to confirm a successful consumption.
 * <p>
 * If enabled, this class will execute the given confirm action {@link Confirmation}.
 * Otherwise no action is taken after successful consumption.
 */
public class ConfirmingDeliveryConsumer implements IncomingDeliveryEventConsumer {

    private final DeliveryConsumer deliveryConsumer;
    private final boolean confirmDelivery;

    @Builder
    ConfirmingDeliveryConsumer(DeliveryConsumer deliveryConsumer, boolean confirmDelivery) {
        this.deliveryConsumer = requireNonNull(deliveryConsumer);
        this.confirmDelivery = confirmDelivery;
    }

    @Override
    public void accept(IncomingDeliveryEvent event, Confirmation action) {
        requireNonNull(event);
        requireNonNull(action);

        deliveryConsumer.consume(event.getDeliveryPackage());

        if (confirmDelivery) {
            action.confirm(this);
        }
    }
}
