package org.amv.trafficsoft.datahub.xfcd.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;

/**
 * An event indicating a successfully confirmation by AMV TrafficSoft
 * of a previously successfully processed delivery package.
 */
@Value
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = ConfirmedDeliveryEvent.Builder.class)
public class ConfirmedDeliveryEvent implements TrafficsoftDeliveryEvent {
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

    }

    @NonNull
    private TrafficsoftDeliveryPackage deliveryPackage;
}
