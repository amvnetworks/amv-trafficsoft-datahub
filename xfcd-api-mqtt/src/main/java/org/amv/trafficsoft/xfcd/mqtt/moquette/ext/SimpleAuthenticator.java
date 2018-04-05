package org.amv.trafficsoft.xfcd.mqtt.moquette.ext;

import com.google.common.base.Charsets;
import io.moquette.spi.security.IAuthenticator;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
public class SimpleAuthenticator implements IAuthenticator {
    private final Map<String, InternalUser> usersByName;
    private final boolean declineAll;

    public SimpleAuthenticator(List<InternalUser> users) {
        this.usersByName = requireNonNull(users).stream()
                .collect(Collectors.toMap(InternalUser::getUsername, val -> val));

        this.declineAll = this.usersByName.isEmpty();

        if (this.declineAll) {
            log.warn("Given user list is empty. All incoming users will be declined.");
        }
    }

    @Override
    public boolean checkValid(String clientId, String username, byte[] password) {
        return checkValid(clientId, username, new String(password, Charsets.UTF_8));
    }

    private boolean checkValid(String clientId, String username, String password) {
        if (declineAll) {
            log.warn("Decline user '{}' from accessing", username);
            return false;
        }

        Optional<InternalUser> internalUser = Optional.ofNullable(usersByName.get(username));

        boolean passwordMatches = internalUser
                .map(InternalUser::getPassword)
                .map(pw -> pw.equalsIgnoreCase(password))
                .orElse(false);

        if (!passwordMatches) {
            log.debug("Declined user {}");
        }
        return passwordMatches;
    }

    @Value
    @Builder
    public static class InternalUser {
        @NonNull
        private String username;
        @NonNull
        private String password;
    }
}
