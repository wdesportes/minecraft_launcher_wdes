package fr.wdes.authentication.custom;

import fr.wdes.authentication.GameProfile;

public class RefreshResponse extends Response {
	protected String accessToken;
	protected String clientToken;
	protected GameProfile selectedProfile;
	protected GameProfile[] availableProfiles;

    public String getAccessToken() {
        return accessToken;
    }

    public GameProfile[] getAvailableProfiles() {
        return availableProfiles;
    }

    public String getClientToken() {
        return clientToken;
    }

    public GameProfile getSelectedProfile() {
        return selectedProfile;
    }
}