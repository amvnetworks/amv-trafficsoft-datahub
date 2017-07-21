package org.amv.trafficsoft.datahub.xfcd.event;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;

/**
 * An event indicating an incoming data package representing
 * a "delivery" from AMV TrafficSoft.
 */
@Value
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = IncomingDeliveryEvent.Builder.class)
public class IncomingDeliveryEvent implements XfcdDeliveryEvent {
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

    }

    @NonNull
    private TrafficsoftDeliveryPackage deliveryPackage;
}