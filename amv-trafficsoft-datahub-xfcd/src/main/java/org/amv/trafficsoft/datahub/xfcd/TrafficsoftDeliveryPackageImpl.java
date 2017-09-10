package org.amv.trafficsoft.datahub.xfcd;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;

import java.util.List;

@Value
@Builder(builderClassName = "Builder", toBuilder = true)
@JsonDeserialize(builder = TrafficsoftDeliveryPackageImpl.Builder.class)
public class TrafficsoftDeliveryPackageImpl implements TrafficsoftDeliveryPackage {
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

    }

    @Singular("addDelivery")
    private List<DeliveryRestDto> deliveries;

    private long contractId;
}
