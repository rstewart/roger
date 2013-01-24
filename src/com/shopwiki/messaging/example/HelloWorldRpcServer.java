package com.shopwiki.messaging.example;

import org.codehaus.jackson.type.TypeReference;

import com.rabbitmq.client.Address;
import com.shopwiki.messaging.*;
import com.shopwiki.messaging.rpc.BasicWorkerFactory;
import com.shopwiki.messaging.rpc.RequestHandler;
import com.shopwiki.messaging.rpc.RpcServer;

/**
 * @owner rstewart
 */
public class HelloWorldRpcServer {

    private static final Address address = new Address("rabbitmq.ny.shopwiki.com");
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
