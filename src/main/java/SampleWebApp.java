import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;

import static io.netty.handler.codec.http.HttpResponseStatus.TEMPORARY_REDIRECT;
import static java.lang.System.getProperty;
import static java.net.URLEncoder.encode;

@Slf4j
@RequiredArgsConstructor
public class SampleWebApp extends AbstractVerticle {
    private final GoogleApiSettings googleApiSettings;
    private final GoogleApiProfileRequestSender googleApiProfileRequestSender;
    private final GoogleApiTokenRequestSender googleApiTokenRequestSender;


    public static void main(String[] args) throws UnsupportedEncodingException {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");

        String callbackUriEnc = encode(getProperty("callbackUri", "http://localhost:8080/google-oauth2-callback"), "UTF-8");

        GoogleApiSettings googleApiSettings = GoogleApiSettings.builder()
                .authCallbackUri(callbackUriEnc)
                .clientId(getProperty("googleClientId"))
                .clientSecret(getProperty("googleClientSecret"))
                .build();

        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(
                new SampleWebApp(
                        googleApiSettings,
                        new GoogleApiProfileRequestSender(vertx),
                        new GoogleApiTokenRequestSender(vertx, googleApiSettings)));
    }

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        SessionStore sessionStore = LocalSessionStore.create(vertx);

        router.route().handler(BodyHandler.create());
        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(sessionStore));

        router.get("/private").handler(ctx -> {
            if (!ctx.session().data().containsKey("email")) {
                ctx.response()
                        .setStatusCode(TEMPORARY_REDIRECT.code())
                        .putHeader("Location", googleApiSettings.getAuthUrl())
                        .end();
            } else {
                ctx.response().end("Secret content for email: " + ctx.session().data().get("email"));
            }
        });

        router.get("/logout").handler(ctx -> {
            ctx.session().destroy();
            ctx.response().end();
        });

        router.get("/google-oauth2-callback").handler(ctx -> {
            String code = ctx.request().params().get("code");

            long start = System.currentTimeMillis();

            googleApiTokenRequestSender.send(code, jsonResp -> {
                String accessToken = jsonResp.getString("access_token");

                googleApiProfileRequestSender.send(accessToken, profileResp -> {
                    String email = profileResp.getJsonArray("emails").getJsonObject(0).getString("value");

                    log.info("logged in as email: {} in {} ms, user: {}", email, System.currentTimeMillis() - start, profileResp);

                    ctx.session().data().put("email", email);
                    ctx.response().setStatusCode(TEMPORARY_REDIRECT.code()).putHeader("Location", "/private").end();
                });
            });

        });

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
}
