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

import org.codehaus.jackson.type.TypeReference;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.shopwiki.roger.*;

/**
 * Run this main before {@link ExampleEventSender}.
 *
 * @author rstewart
 */
public class ExampleEventHandler {

    private static final Address address = new Address("localhost");
    public static final RabbitConnector connector = new RabbitConnector(address);
    public static final Route route = new Route("events", "names");

    public static void main(String[] args) throws Exception {

        MessageHandler<String> handler = new MessageHandler<String>() {
            @Override
            public TypeReference<String> getMessageType() {
                return new TypeReference<String>() {};
            }

            @Override
            public void handleMessage(String name, BasicProperties properties) {
                System.out.println("Hello " + name + "!");
            }
        };

        // Create the exchange if it doesn't exist.
        {
            Connection conn = connector.getConnection(1);
            Channel channel = conn.createChannel();
            channel.exchangeDeclare(route.exchange, "topic");
            conn.close();
        }

        boolean daemon = false;

        MessageWorker<String> worker = new MessageWorker<String>(connector, handler, route, daemon);
        worker.start();
    }
}
