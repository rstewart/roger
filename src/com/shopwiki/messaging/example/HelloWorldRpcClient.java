package com.shopwiki.messaging.example;

import java.util.concurrent.Future;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.shopwiki.messaging.Route;
import com.shopwiki.messaging.rpc.RpcClient;
import com.shopwiki.messaging.rpc.RpcResponse;

/**
 * @author Rob
 */
public class HelloWorldRpcClient {

    public static void main(String[] args) throws Exception {
        Connection conn = HelloWorldRpcServer.connector.getConnection(1);
        Channel channel = conn.createChannel();
        Route route = new Route("", "HelloWorld");
        RpcClient client = new RpcClient(channel, route, false);
        Future<RpcResponse> future = client.sendRequest("Robert");
        RpcResponse response = future.get();
        System.out.println("HEADERS:\n" + response.getHeaders());
        System.out.println();
        System.out.println("BODY:\n" + response.getBody());
    }
}
