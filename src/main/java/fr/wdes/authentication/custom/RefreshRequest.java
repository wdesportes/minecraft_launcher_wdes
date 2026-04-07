package fr.wdes.authentication.custom;

import fr.wdes.authentication.GameProfile;

public class RefreshRequest {
	protected final String clientToken;
	protected final String accessToken;
	protected final GameProfile selectedProfile;

    public RefreshRequest(final YggdrasilAuthenticationService authenticationService) {
        this(authenticationService, null);
    }

    public RefreshRequest(final YggdrasilAuthenticationService authenticationService, final GameProfile profile) {
        clientToken = authenticationService.getClientToken();
        accessToken = authenticationService.getAccessToken();
        selectedProfile = profile;
    }
}