package org.amv.trafficsoft.xfcd.consumer.jdbc;

import com.google.common.base.Strings;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

public class TrafficsoftXfcdJdbcPropertiesValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return TrafficsoftXfcdJdbcProperties.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        checkArgument(supports(target.getClass()), "Unsupported type.");

        TrafficsoftXfcdJdbcProperties properties = (TrafficsoftXfcdJdbcProperties) target;

        if (properties.getJdbcUrl() == null || Strings.isNullOrEmpty(properties.getJdbcUrl())) {
            errors.rejectValue("jdbcUrl", "jdbcUrl.empty", "Empty jdbc url.");
        }

        final boolean locationGiven = !Strings.isNullOrEmpty(properties.getFlywayScriptsLocation());
        if (locationGiven && properties.isSchemaMigrationEnabled()) {
            final String flywayScriptsLocation = properties.getFlywayScriptsLocation();
            final Optional<URL> resource = Optional.ofNullable(this.getClass().getResource(flywayScriptsLocation));
            if ((resource.isPresent())) {

                try {
                    final File file = Paths.get(resource.get().toURI()).toFile();
                    if (!file.exists()) {
                        errors.rejectValue("flywayScriptsLocation", "flywayScriptsLocation.invalid", "Location does not exist.");
                    }
                    if (!file.canRead()) {
                        errors.rejectValue("flywayScriptsLocation", "flywayScriptsLocation.readonly", "Location is not readable.");
                    }
                } catch (URISyntaxException e) {
                    errors.rejectValue("flywayScriptsLocation", "flywayScriptsLocation.invalid", "Location is malformed.");
                }
            }
        }
    }
}
