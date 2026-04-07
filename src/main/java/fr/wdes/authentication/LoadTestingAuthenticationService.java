package fr.wdes.authentication;

import java.io.File;
import java.util.Map;

import com.google.gson.JsonElement;

import fr.wdes.logger;
import fr.wdes.authentication.custom.YggdrasilAuthenticationService;
import fr.wdes.authentication.exceptions.AuthenticationException;
import fr.wdes.events.AuthenticationChangedListener;


public class LoadTestingAuthenticationService implements AuthenticationService {
    private final AuthenticationService primary = new YggdrasilAuthenticationService();

    public void addAuthenticationChangedListener(final AuthenticationChangedListener listener) {
        primary.addAuthenticationChangedListener(listener);
    }

    public boolean canLogIn() {
        return primary.canLogIn();
    }

    public boolean canPlayOnline() {
        return primary.canPlayOnline();
    }

    public GameProfile[] getAvailableProfiles() {
        return primary.getAvailableProfiles();
    }

    public GameProfile getSelectedProfile() {
        return primary.getSelectedProfile();
    }

    public String getSessionToken() {
        return primary.getSessionToken();
    }

    public String getUsername() {
        return primary.getUsername();
    }

    public String guessPasswordFromSillyOldFormat(final File lastlogin) {
        return primary.guessPasswordFromSillyOldFormat(lastlogin);
    }

    public boolean isLoggedIn() {
        return primary.isLoggedIn();
    }

    public void loadFromStorage(final Map<String, String> credentials) {
        primary.loadFromStorage(credentials);
    }

    public void logIn() throws AuthenticationException {
        primary.logIn();
    }

    public void logOut() {
        primary.logOut();
    }

    public void removeAuthenticationChangedListener(final AuthenticationChangedListener listener) {
        primary.removeAuthenticationChangedListener(listener);
    }

    public Map<String, String> saveForStorage() {
        return primary.saveForStorage();
    }

    public void selectGameProfile(final GameProfile profile) throws AuthenticationException {
        primary.selectGameProfile(profile);
    }

    public void setPassword(final String password) {
        primary.setPassword(password);
    }

    public void setRememberMe(final boolean rememberMe) {
        primary.setRememberMe(rememberMe);
    }

    public void setUsername(final String username) {
        primary.setUsername(username);
    }

    public boolean shouldRememberMe() {
        return primary.shouldRememberMe();
    }

	public JsonElement getUserProperties() {

		return null;
	}
}
