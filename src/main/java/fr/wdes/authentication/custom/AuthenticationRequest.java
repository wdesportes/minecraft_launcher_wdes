package fr.wdes.authentication.custom;

public class AuthenticationRequest {

	protected final Agent agent;
    protected final String username;
    protected final String password;
    protected final String clientToken;

    public AuthenticationRequest(final YggdrasilAuthenticationService authenticationService, final String password) {
        agent = authenticationService.getAgent();
        username = authenticationService.getUsername();
        clientToken = authenticationService.getClientToken();
        this.password = password;
    }
}