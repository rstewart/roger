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
    private final Channel channel;
    private final Route route;
    private final String queueName;

    // TODO: Allow this to take multiple Channels, the same way RequestConsumer does ???
    public MessageConsumer(MessageHandler<T> handler, Channel channel, Map<String,Object> queueArgs, Route route) throws IOException {
        super(channel);
        this.handler = handler;
        this.messageType = handler.getMessageType();
        this.channel = channel;
        this.route = route;
        this.queueName = QueueUtil.declareAnonymousQueue(channel, queueArgs).getQueue();
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
