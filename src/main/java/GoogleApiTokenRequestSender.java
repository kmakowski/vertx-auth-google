import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GoogleApiTokenRequestSender {
    private final String tokenRequestPath = "/oauth2/v3/token";
    private final Vertx vertx;
    private final GoogleApiSettings apiSettings;

    public void send(final String code, final Handler<JsonObject> handler) {
        HttpClientRequest tokenReq = vertx.createHttpClient(new HttpClientOptions().setSsl(true))
                .post(GoogleApiSettings.PORT, GoogleApiSettings.HOST, tokenRequestPath,
                        tokenResp -> tokenResp.bodyHandler(
                                body -> handler.handle(new JsonObject(body.toString()))));
        tokenReq.putHeader("Content-Type", "application/x-www-form-urlencoded");
        String params = "grant_type=authorization_code&code=" + code + "&client_id=" + apiSettings.getClientId()
                + "&client_secret=" + apiSettings.getClientSecret() +
                "&redirect_uri=" + apiSettings.getAuthCallbackUri();

        tokenReq.end(params);
    }

}
