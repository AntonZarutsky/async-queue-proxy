package com.proto.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import static com.proto.vertx.MessageQueueVerticle.QUEUE_POllED_MESSAGE;
import static com.proto.vertx.MessageQueueVerticle.QUEUE_REPLY_MESSAGE;

@Slf4j
public class DownstreamHttpRequestProcessor extends AbstractVerticle {

  private HttpServer httpServer;

  @Override
  public void start() throws Exception {
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.get("/down").handler(this::handleGet);
    router.put("/down/:id").handler(this::handlePut);

    httpServer =
        vertx
            .createHttpServer()
            .requestHandler(router::accept)
            .listen(
                8082,
                res -> {
                  if (res.succeeded()) {
                    log.info("Server is ready!");
                  } else {
                    log.info("Failed to bind!");
                    log.info("{}", res);
                  }
                });
  }

  @Override
  public void stop() throws Exception {
    httpServer.close();
  }

  private void handleGet(RoutingContext routingContext) {
    log.info("Downstream::get");

    val response = routingContext.response();

    vertx
        .eventBus()
        .send(
            QUEUE_POllED_MESSAGE,
            "",
            event -> {
              if (event.failed()) {
                log.info("Fetching ->, {}", event.cause().getMessage());
                response.setStatusCode(404);
                response.end(event.cause().getMessage());
              } else {
                log.info("Fetched ->, {}", event.result().body());
                response.end(event.result().body().toString());
              }
            });
  }

  private void handlePut(RoutingContext routingContext) {
    log.info("Downstream::put");
    //
    val response = routingContext.response();
    String id = routingContext.request().getParam("id");
    log.info("Downstream::put. id={}", id);

    val body = routingContext.getBodyAsString();
    log.info("Downstream::put. body {}", body);
    vertx.eventBus().publish(QUEUE_REPLY_MESSAGE, new MyMessageEvent(Long.parseLong(id), body));

    response.end("OK");
  }
}
