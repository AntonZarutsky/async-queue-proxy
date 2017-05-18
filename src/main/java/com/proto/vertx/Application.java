package com.proto.vertx;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class Application {

  public static void main(String[] args) throws Exception {
    Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(2));
    vertx.eventBus().registerDefaultCodec(MyMessageEvent.class, new MyMessageEventCodec());


    vertx.deployVerticle("com.proto.vertx.UpstreamHttpRequestProcessor", new DeploymentOptions().setInstances(4));
    vertx.deployVerticle("com.proto.vertx.DownstreamHttpRequestProcessor", new DeploymentOptions().setInstances(4));
    vertx.deployVerticle("com.proto.vertx.MessageQueueVerticle");

  }
}
