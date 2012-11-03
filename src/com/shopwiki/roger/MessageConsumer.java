package com.shopwiki.roger;

import java.io.IOException;

import org.codehaus.jackson.type.TypeReference;

import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.BasicProperties;

/**
 * @owner rstewart
 */
public class MessageConsumer<T> extends DefaultConsumer {

    private static final boolean DEBUG = MessagingUtil.DEBUG;

    private final MessageHandler<T> handler;
    private final TypeReference<T> messageType;
    private final Channel channel;
    private final Route route;
    private final String queueName;

    public MessageConsumer(MessageHandler<T> handler, Channel channel, Route route) throws IOException {
        super(channel);
        this.handler = handler;
        this.messageType = handler.getMessageType();
        this.channel = channel;
        this.route = route;
        this.queueName = MessagingUtil.declareAnonymousQueue(channel).getQueue();
    }

    public String getQueueName() {
        return queueName;
    }

    public void start() throws IOException {
        if (route != null) {
            channel.queueBind(queueName, route.exchange, route.key);
        }

        channel.basicConsume(queueName, true, this);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
        T message = MessagingUtil.getDeliveryBody(body, messageType);
        if (DEBUG) {
            System.out.println("*** MessageConsumer " + MessageConsumer.this.getClass().getName() + " RECEIVED MESSAGE ***");
            System.out.println("*** consumerTag: " + consumerTag);
            System.out.println("*** envelope:\n" + MessagingUtil.prettyPrint(envelope));
            System.out.println("*** properties:\n" + MessagingUtil.prettyPrint(properties));
            System.out.println("*** message: " + MessagingUtil.prettyPrintMessage(message));
        }
        handler.handleMessage(message, properties);
        // AUTO-ACKING
    }
}
