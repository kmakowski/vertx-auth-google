import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GoogleApiSettings {
    public static final String HOST = "www.googleapis.com";
    public static final int PORT = 443;
    private String clientId;
    private String clientSecret;
    private String authCallbackUri;

    public String getAuthUrl() {
        return "https://accounts.google.com/o/oauth2/auth?scope=email%20profile" +
                "&response_type=code&client_id=" + getClientId() +
                "&redirect_uri=" + getAuthCallbackUri();
    }
}
