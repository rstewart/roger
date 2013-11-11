/*
 * Copyright [2013] [ShopWiki]
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

package com.shopwiki.roger.example;

import java.io.IOException;

import org.codehaus.jackson.type.TypeReference;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.shopwiki.roger.*;
import com.shopwiki.roger.event.MessageHandler;
import com.shopwiki.roger.event.MessagingManager;

/**
 * Run this main before {@link ExampleEventSender}.
 *
 * @author rstewart
 */
public class ExampleEventHandler {

    public static final Route ROUTE = new Route(ExampleConstants.EXCHANGE, "example-event-routing-key");

    public static final MessagingManager manager = new MessagingManager(ExampleConstants.CONNECTOR, 1, 10);

    public static volatile String lastMessage = null;

    public static final MessageHandler<String> handler = new MessageHandler<String>() {
        @Override
        public TypeReference<String> getMessageType() {
            return new TypeReference<String>() {};
        }

        @Override
        public void handleMessage(String name) {
            lastMessage = "Hello " + name + "!";
            System.out.println(lastMessage);
        }
    };

    /**
     * Create the exchange if it doesn't exist.
     */
    public static void declareExchange() throws IOException {
        Connection conn = null;
        try {
            conn = ExampleConstants.CONNECTOR.newConnection(1);
            Channel channel = conn.createChannel();
            channel.exchangeDeclare(ROUTE.exchange, "topic");
        } finally {
            RabbitConnector.closeConnection(conn);
        }
    }

    public static void main(String[] args) throws Exception {
        declareExchange();
        manager.start();
        manager.add(handler, ROUTE);
    }
}
