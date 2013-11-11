/*
 * Copyright [2012] [ShopWiki]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shopwiki.roger.event;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.type.TypeReference;

import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.shopwiki.roger.MessagingUtil;
import com.shopwiki.roger.QueueUtil;
import com.shopwiki.roger.Route;

/**
 * Implementation of the RabbitMQ client's Consumer interface.
 * The user is not expected to use this class directly.
 * See {@link MessageWorker} instead.
 *
 * @author rstewart
 */
public class MessageConsumer<T> extends DefaultConsumer {

    private static final boolean DEBUG = MessagingUtil.DEBUG;

    private final MessageHandler<T> handler;
    private final TypeReference<T> messageType;

    public static <T> void start(MessageHandler<T> handler, List<Channel> channels, Map<String,Object> queueArgs, Route route) throws IOException {
        Channel channel0 = channels.get(0);
        String queuePrefix = routeToQueuePrefix(route);
        String queueName = QueueUtil.declareAnonymousQueue(channel0, queuePrefix, queueArgs).getQueue();

        if (route != null) {
            channel0.queueBind(queueName, route.exchange, route.key);
        }

        for (Channel channel : channels) {
            MessageConsumer<T> consumer = new MessageConsumer<T>(handler, channel);
            channel.basicConsume(queueName, true, consumer); // AUTO-ACKING
        }
    }

    private MessageConsumer(MessageHandler<T> handler, Channel channel) {
        super(channel);
        this.handler = handler;
        this.messageType = handler.getMessageType();
    }

    private static String routeToQueuePrefix(Route route) {
        if (route.exchange == null || route.exchange.isEmpty()) {
            return route.key;
        }
        return route.exchange + "-" + route.key;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
        T message = MessagingUtil.getDeliveryBody(body, messageType);
        if (DEBUG) {
            System.out.println("*** MessageConsumer " + handler.getClass().getCanonicalName() + " RECEIVED MESSAGE ***");
            System.out.println("*** consumerTag: " + consumerTag);
            System.out.println("*** envelope:\n" + MessagingUtil.prettyPrint(envelope));
            System.out.println("*** properties:\n" + MessagingUtil.prettyPrint(properties));
            System.out.println("*** message: " + MessagingUtil.prettyPrintMessage(message));
        }
        handler.handleMessage(message);
        // AUTO-ACKING
    }
}
