package com.proto.vertx;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class MessageQueueVerticle extends AbstractVerticle {

  static AtomicLong counter = new AtomicLong();

  @Getter
  @AllArgsConstructor()
  class MyMessage {
    long id;
    Message message;

    public MyMessage(Message message) {
      this.id = counter.incrementAndGet();
      this.message = message;
    }
  }

  public static final String QUEUE_PUSH_MESSAGE = "queue.message.push";
  public static final String QUEUE_POllED_MESSAGE = "queue.message.poll";
  public static final String QUEUE_REPLY_MESSAGE = "queue.message.reply";

  private LinkedList<MyMessage> queue = Lists.newLinkedList();
  private Map<Long, MyMessage> map = Maps.newHashMap();

  @Override
  public void start() throws Exception {
    vertx
        .eventBus()
        .consumer(
            QUEUE_PUSH_MESSAGE,
            event -> {
              log.info("Pushed =, {}", event.body());
              val message = new MyMessage(event);
              queue.push(message);
              map.put(message.getId(), message);
            });

    vertx
        .eventBus()
        .consumer(
            QUEUE_REPLY_MESSAGE,
            event -> {
              log.info("Replied =, {}", event.body());
              val result = map.get(((MyMessageEvent) event.body()).getId());
              if (result != null) {
                map.remove(event.body());
                queue.remove(event.body());
                result.getMessage().reply(((MyMessageEvent) event.body()).getBody());
              }
            });

    vertx
        .eventBus()
        .consumer(
            QUEUE_POllED_MESSAGE,
            event -> {
              log.info("Polled =, {}", event.body());
              val result = queue.pollLast();
              if (result != null) {
                event.reply(new MyMessageEvent(result.id, result.getMessage().body().toString()));
              } else {
                event.fail(404, "No Message In Queue");
              }
            });
  }

}
