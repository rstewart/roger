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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.shopwiki.roger.MessagingUtil;
import com.shopwiki.roger.RabbitConnector;

/**
 * Run this main after {@link ExampleEventHandler}.
 *
 * @author rstewart
 */
public class ExampleEventSender {

    public static void sendEvent() throws IOException {
        Connection conn = null;
        try {
            conn = ExampleConstants.CONNECTOR.getConnection(1);
            Channel channel = conn.createChannel();
            MessagingUtil.sendMessage(channel, ExampleEventHandler.ROUTE, "Robert");
        } finally {
            RabbitConnector.closeConnection(conn);
        }
        System.out.println("ExampleEventSender DONE!");
    }

    public static void main(String[] args) throws Exception {
        sendEvent();
    }
}
