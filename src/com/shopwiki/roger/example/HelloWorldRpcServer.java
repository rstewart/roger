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

package com.shopwiki.roger.example;

import org.codehaus.jackson.type.TypeReference;

import com.rabbitmq.client.Address;
import com.shopwiki.roger.*;
import com.shopwiki.roger.rpc.BasicWorkerFactory;
import com.shopwiki.roger.rpc.RequestHandler;
import com.shopwiki.roger.rpc.RpcServer;

/**
 * @owner rstewart
 */
public class HelloWorldRpcServer {

    private static final Address address = new Address("localhost");
    public static final MessagingConnector connector = new MessagingConnector(address);

    public static void main(String[] args) throws Exception {

        RequestHandler<String, MapMessage> handler = new RequestHandler<String, MapMessage>() {
            @Override
            public TypeReference<String> getRequestType() {
                return new TypeReference<String>() {};
            }

            @Override
            public MapMessage handleRequest(String name) throws Exception {
                MapMessage response = new MapMessage();
                response.put("greeting", "Hello " + name + "!");
                return response;
            }
        };

        BasicWorkerFactory factory = new BasicWorkerFactory(connector, 2);
        factory.addHandler("HelloWorld", handler);

        RpcServer server = new RpcServer(factory, "");
        server.start();
    }
}
