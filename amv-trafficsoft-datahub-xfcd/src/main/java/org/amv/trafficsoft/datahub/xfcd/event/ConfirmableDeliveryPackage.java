package org.amv.trafficsoft.datahub.xfcd.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;

@Value
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = ConfirmableDeliveryPackage.Builder.class)
public class ConfirmableDeliveryPackage {
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

    }

    @NonNull
    private TrafficsoftDeliveryPackage deliveryPackage;
}
