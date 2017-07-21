package org.amv.trafficsoft.datahub.xfcd.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;

/**
 * An event indicating a successfully processed delivery package
 * which subsequently must be confirmed by AMV TrafficSoft.
 */
@Value
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = ConfirmableDeliveryEvent.Builder.class)
public class ConfirmableDeliveryEvent implements XfcdDeliveryEvent {
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

    }

    @NonNull
    private TrafficsoftDeliveryPackage deliveryPackage;
}
