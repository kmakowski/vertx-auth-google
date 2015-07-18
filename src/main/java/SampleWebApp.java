import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SampleWebApp extends AbstractVerticle {

    public static void main(String[] args) {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        Vertx.vertx().deployVerticle(new SampleWebApp());
    }

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        SessionStore sessionStore = LocalSessionStore.create(vertx);

        router.route().handler(BodyHandler.create());
        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(sessionStore));

        router.route("/test/:id").handler(ctx -> {
            ctx.response().end(ctx.getBodyAsString());
        });

        router.route("/test2").handler(ctx -> {
            log.info("session id {}, ", ctx.session().id());

            sessionStore.size(size -> {
                log.info("session size {}", size.result());

                Session session = Session.builder()
                        .size(size.result())
                        .build();

                ctx.response().end(Json.encode(session));
            });
        });

        router.route().handler(StaticHandler.create());
        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
}
