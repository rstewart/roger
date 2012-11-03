package com.shopwiki.messaging;

import java.io.IOException;

import com.rabbitmq.client.*;
import com.shopwiki.messaging.MessagingReconnector.*;

/**
 * @owner rstewart
 */
public class MessageWorker<T> {

    private final MessagingConnector connector;
    private final MessageHandler<T> handler;
    private final Route route;
    public final MessagingReconnector reconnector;

    public MessageWorker(MessagingConnector connector, MessageHandler<T> handler, Route route, ReconnectLogger reconnectLogger) {
        this.connector = connector;
        this.handler = handler;
        this.route = route;

        ReconnectHandler reconnectHandler = new ReconnectHandler() {
            @Override
            public boolean reconnect() throws Exception {
                start();
                return true;
            }
        };

        reconnector = new MessagingReconnector(reconnectHandler, reconnectLogger, 10);
    }

    private volatile Channel channel;

    public void start() throws IOException {
        Connection conn = connector.getConnection(1);
        conn.addShutdownListener(reconnector);
        channel = conn.createChannel();

        MessageConsumer<T> consumer = new MessageConsumer<T>(handler, channel, route);
        consumer.start();
    }

    public void sendMessage(T message) throws IOException {
        MessagingUtil.sendMessage(channel, route, message);
    }
}
