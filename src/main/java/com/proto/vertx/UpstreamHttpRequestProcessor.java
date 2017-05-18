package com.proto.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

import static com.proto.vertx.MessageQueueVerticle.QUEUE_PUSH_MESSAGE;

@Slf4j
public class UpstreamHttpRequestProcessor extends AbstractVerticle {

  private HttpServer httpServer;

  @Override
  public void start() throws Exception {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.get("/up/:id").handler(this::handleGet);
    httpServer =
        vertx
            .createHttpServer()
            .requestHandler(router::accept)
            .listen(
                8081,
                res -> {
                  if (res.succeeded()) {
                    log.info("Server is ready!");
                  } else {
                    log.info("Failed to bind!");
                  }
                });
  }

  @Override
  public void stop() throws Exception {
    httpServer.close();
  }

  private void handleGet(RoutingContext routingContext) {
    String id = routingContext.request().getParam("id");
    HttpServerResponse response = routingContext.response();

    log.info("Server <-, {}", id);
    vertx
        .eventBus()
        .send(
            QUEUE_PUSH_MESSAGE,
            id,
            event -> {
              log.info("Server ->, {}", id);
              if (event.succeeded()) {
                response.end(event.result().body().toString());
              } else {
                response.setStatusCode(500);
                response.end(event.cause().getMessage());
              }
            });
  }
}
