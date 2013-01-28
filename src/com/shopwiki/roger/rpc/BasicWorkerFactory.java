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

import java.io.IOException;
import java.util.*;

import com.rabbitmq.client.*;
import com.shopwiki.roger.MessagingConnector;
import com.shopwiki.roger.rpc.RpcServer.WorkerFactory;

/**
 * @owner rstewart
 */
public class BasicWorkerFactory implements WorkerFactory {

    private final Map<String, RequestHandler<?,?>> nameToHandler = new LinkedHashMap<String, RequestHandler<?,?>>();

    private final MessagingConnector connector;
    private final int numThreads;

    public BasicWorkerFactory(MessagingConnector connector, int numThreads) {
        this.connector = connector;
        this.numThreads = numThreads;
    }

    public void addHandler(String name, RequestHandler<?,?> handler) {
        nameToHandler.put(name, handler);
    }

    public Map<String, RequestHandler<?,?>> getHandlerMap() {
        return Collections.unmodifiableMap(nameToHandler);
    }

    @Override
    public RpcWorkers createWorkers(String queuePrefix) throws IOException {
        Connection conn = connector.getLongConnection(numThreads);

        List<Channel> channels = new ArrayList<Channel>();
        for (int i = 0; i < numThreads; i++) {
            Channel channel = conn.createChannel();
            channel.basicQos(1);
            channels.add(channel);
        }

        RpcWorkers workers = new RpcWorkers(conn);

        for (String procedureName : nameToHandler.keySet()) {
            RequestHandler<?,?> handler = nameToHandler.get(procedureName);
            RpcWorker worker = new RpcWorker(handler, channels, queuePrefix, procedureName);
            workers.add(worker);
        }

        return workers;
    }
}
