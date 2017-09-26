package org.amv.trafficsoft.xfcd.consumer.jdbc;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Optional;

@Value
@Builder(toBuilder = true)
public class TrafficsoftXfcdXfcdEntity {
    private long nodeId;
    @NonNull
    private String type;
    private BigDecimal value;
    private String valueAsString;

    public Optional<BigDecimal> getValue() {
        return Optional.ofNullable(value);
    }

    public Optional<String> getValueAsString() {
        return Optional.ofNullable(valueAsString);
    }
}
