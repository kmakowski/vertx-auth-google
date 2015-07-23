import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GoogleApiProfileRequestSender {
    private final String profileRequestPath = "/plus/v1/people/me?access_token=";
    private final Vertx vertx;

    public void send(final String accessToken, final Handler<JsonObject> handler) {
        vertx.createHttpClient(new HttpClientOptions().setSsl(true))
                .getNow(GoogleApiSettings.PORT, GoogleApiSettings.HOST, profileRequestPath + accessToken,
                        resp -> resp.bodyHandler(
                                body -> handler.handle(new JsonObject(body.toString()))));
    }

}
