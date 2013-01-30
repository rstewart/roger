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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.shopwiki.roger.Route;
import com.shopwiki.roger.rpc.RpcClient;
import com.shopwiki.roger.rpc.RpcResponse;

/**
 * Run this main after {@link ExampleRpcServer}.
 *
 * @author rstewart
 */
public class ExampleRpcClient {

    public static void main(String[] args) throws Exception {
        Connection conn = ExampleRpcServer.connector.getConnection(1);
        Channel channel = conn.createChannel();
        Route route = new Route("", "HelloWorld");
        Map<String,Object> queueArgs = null;
        RpcClient client = new RpcClient(channel, route, queueArgs, false);
        Future<RpcResponse> future = client.sendRequest("Robert");
        RpcResponse response = future.get();
        System.out.println("HEADERS:\n" + response.getHeaders());
        System.out.println();
        System.out.println("BODY:\n" + response.getBody());
    }
}
