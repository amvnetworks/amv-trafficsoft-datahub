package org.amv.trafficsoft.xfcd.consumer.jdbc;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Optional;

@Value
@Builder(toBuilder = true)
public class TrafficsoftXfcdStateEntity {
    private long nodeId;
    @NonNull
    private String code;
    private String value;

    public Optional<String> getValue() {
        return Optional.ofNullable(value);
    }
}
