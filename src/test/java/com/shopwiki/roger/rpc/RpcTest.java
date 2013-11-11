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

package com.shopwiki.roger.rpc;

import org.junit.Assert;
import org.junit.Test;

import com.shopwiki.roger.example.ExampleRpcClient;
import com.shopwiki.roger.example.ExampleRpcServer;
import com.shopwiki.roger.example.ExampleRpcServer.Response;

/**
 * @author rstewart
 */
public class RpcTest {

    @Test
    public void test() throws Exception {
        RpcServer server = ExampleRpcServer.createRpcServer();
        server.start();

        RpcResponse<Response> response = ExampleRpcClient.sendRequest("Robert");
        server.stop();

        Assert.assertEquals(ResponseStatus.OK, response.getStatus());
        Assert.assertEquals("Hello Robert!", response.getBody().greeting);
    }
}
