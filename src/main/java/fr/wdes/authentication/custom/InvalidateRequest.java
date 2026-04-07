package fr.wdes.authentication.custom;
@SuppressWarnings("unused")
public class InvalidateRequest {
    private final String accessToken;
    private final String clientToken;

    public InvalidateRequest(final YggdrasilAuthenticationService authenticationService) {
        accessToken = authenticationService.getAccessToken();
        clientToken = authenticationService.getClientToken();
    }
}