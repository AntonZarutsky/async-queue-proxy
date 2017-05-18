package com.proto.vertx;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class MyMessageEvent {
  long id;
  String body;
}
