package com.shopwiki.messaging;

import org.codehaus.jackson.type.TypeReference;

import com.rabbitmq.client.AMQP.BasicProperties;

/**
 * @owner rstewart
 */
public interface MessageHandler<T> {

    // TODO: WHY doesn't this work ???
    //private final TypeReference<T> typeRef = new TypeReference<T>() { };
    // ...oh Well, make them implement one more method.
    public TypeReference<T> getMessageType();

    public void handleMessage(T message, BasicProperties properties);
}
