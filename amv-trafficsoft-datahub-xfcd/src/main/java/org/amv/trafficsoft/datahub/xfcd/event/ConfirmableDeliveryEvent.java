package org.amv.trafficsoft.datahub.xfcd.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;

@Value
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = ConfirmableDeliveryEvent.Builder.class)
public class ConfirmableDeliveryEvent implements XfcdEvent {
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

    }

    @NonNull
    private TrafficsoftDeliveryPackage deliveryPackage;
}
