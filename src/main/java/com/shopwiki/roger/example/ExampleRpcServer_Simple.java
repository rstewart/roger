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
import com.shopwiki.roger.*;
import com.shopwiki.roger.rpc.BasicWorkerFactory;
import com.shopwiki.roger.rpc.RequestHandler;
import com.shopwiki.roger.rpc.RpcServer;

/**
 * Simplified RPC example.
 *
 * @author rstewart
 */
public class ExampleRpcServer_Simple {

    public static class Request {
        public String name;
    }

    public static class Response {
        public final String greeting;
        public Response(String greeting) { this.greeting = greeting; }
    }

    public static void main(String[] args) throws Exception {

        RequestHandler<Request, Response> handler = new RequestHandler<Request, Response>() {
            @Override
            public TypeReference<Request> getRequestType() {
                return new TypeReference<Request>() {};
            }

            @Override
            public Response handleRequest(Request request) throws Exception {
                return new Response("Hello " + request.name + "!");
            }
        };

        RabbitConnector connector = new RabbitConnector(new Address("localhost"));

        BasicWorkerFactory factory = new BasicWorkerFactory(connector, 2);
        factory.addHandler("HelloWorld", handler);

        RpcServer server = new RpcServer(factory, "RpcExample_");
        server.start();
    }
}
