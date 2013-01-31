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

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.type.TypeReference;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.shopwiki.roger.*;
import com.shopwiki.roger.RabbitReconnector.ReconnectLogger;
import com.shopwiki.roger.rpc.BasicWorkerFactory;
import com.shopwiki.roger.rpc.PostProcessors;
import com.shopwiki.roger.rpc.RequestHandler;
import com.shopwiki.roger.rpc.RpcServer;
import com.shopwiki.roger.rpc.RpcServer.QueueDeclarator;
import com.shopwiki.roger.rpc.RpcWorker;

/**
 * Run this main before {@link ExampleRpcClient}.
 *
 * @author rstewart
 */
public class ExampleRpcServer {

    public static class Request {
        public String name;
    }

    public static class Response {
        public final String greeting;
        public Response(String greeting) { this.greeting = greeting; }
    }

    private static final Address address = new Address("localhost");
    public static final RabbitConnector connector = new RabbitConnector(address);

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

        BasicWorkerFactory factory = new BasicWorkerFactory(connector, 2);
        factory.addHandler("HelloWorld", handler);

        /*
         * NOTE: The QueueDeclarator is optional.
         * You can set it to null and just create the queue manually.
         * by going to http://localhost:55672/#/queues
         */
        QueueDeclarator queueDeclarator = new QueueDeclarator() {
            @Override
            public void declareQueue(Channel channel, RpcWorker worker) throws IOException {
                Map<String,Object> queueArgs = null;
                QueueUtil.declareNamedQueue(channel, worker.getQueueName(), queueArgs);
            }

            @Override
            public void bindQueue(Channel channel, RpcWorker worker) throws IOException {
                // Not using any routing-key
            }
        };

        String queuePrefix = "RpcExample_";
        PostProcessors postProcessors = null;
        ReconnectLogger reconnectLogger = null;

        RpcServer server = new RpcServer(factory, queuePrefix, queueDeclarator, postProcessors, reconnectLogger);
        server.start();
    }
}
