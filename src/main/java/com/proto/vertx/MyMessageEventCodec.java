package com.proto.vertx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class MyMessageEventCodec implements MessageCodec<MyMessageEvent, MyMessageEvent> {

  ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void encodeToWire(Buffer buffer, MyMessageEvent messageEvent) {
    byte[] encoded = new byte[0];
    try {
      encoded = objectMapper.writeValueAsBytes(messageEvent);
    } catch (JsonProcessingException e) {
      log.error("", e);
    }

    buffer.appendInt(encoded.length);
    Buffer buff = Buffer.buffer(encoded);
    buffer.appendBuffer(buff);
  }

  @Override
  public MyMessageEvent decodeFromWire(int pos, Buffer buffer) {
    try {
      int length = buffer.getInt(pos);
      pos += 4;
      byte[] encoded = buffer.getBytes(pos, pos + length);

      return objectMapper.readerFor(MyMessageEvent.class).readValue(encoded);
    } catch (IOException e) {
      log.error("", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public MyMessageEvent transform(MyMessageEvent messageEvent) {
    return messageEvent;
  }

  @Override
  public String name() {
    return "myMessageEvent";
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
