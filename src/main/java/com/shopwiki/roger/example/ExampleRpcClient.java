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

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.type.TypeReference;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.shopwiki.roger.MessagingUtil;
import com.shopwiki.roger.Route;
import com.shopwiki.roger.example.ExampleRpcServer.Request;
import com.shopwiki.roger.example.ExampleRpcServer.Response;
import com.shopwiki.roger.rpc.RpcClient;
import com.shopwiki.roger.rpc.RpcResponse;

/**
 * Run this main after {@link ExampleRpcServer}.
 *
 * @author rstewart
 */
public class ExampleRpcClient {

    public static void main(String[] args) throws Exception {
        Connection conn = ExampleRpcServer.connector.getDaemonConnection(1);
        Channel channel = conn.createChannel();
        Route route = new Route("", "RpcExample_HelloWorld");
        Map<String,Object> queueArgs = null;

        TypeReference<Response> responseType = new TypeReference<Response>() { };
        RpcClient<Response> client = RpcClient.create(channel, route, queueArgs, responseType);

        Request request = new Request();
        request.name = "Robert";

        Future<RpcResponse<Response>> future = client.sendRequest(request);
        RpcResponse<Response> response = future.get(5, TimeUnit.SECONDS);

        System.out.println("HEADERS:\n" + response.getHeaders());
        System.out.println();
        System.out.println("BODY:\n" + MessagingUtil.prettyPrintMessage(response.getBody()));
    }
}
