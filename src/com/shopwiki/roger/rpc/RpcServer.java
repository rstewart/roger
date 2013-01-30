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

import com.rabbitmq.client.*;
import com.shopwiki.roger.RabbitReconnector;
import com.shopwiki.roger.RabbitReconnector.*;

/**
 * The main entry point for creating & starting an RPC server using Roger.
 *
 * @author rstewart
 */
public class RpcServer {

    /**
     * Used by {@link RpcServer} to create {@link RpcWorker}s.
     *
     * A standard implementation is provided: {@link BasicWorkerFactory}.
     * However, the user may create their own implementation if they
     * desire more control over how {@link Channel}s are allocated to {@link RpcWorker}s.
     */
    public interface WorkerFactory {
        RpcWorkers createWorkers(String queuePrefix) throws IOException;
    }

    // TODO: Rename this QueueManager ???
    // TODO: Provide a standard implementation similar to ShopWiki's ???
    /**
     * No implementation is required.
     * You only need to provide one if you want an {@link RpcServer} to programmatically
     * create/check your RPC queues & routing-key bindings.
     */
    public static interface QueueDeclarator {
        void declareQueue(Channel channel, RpcWorker worker) throws IOException;
        void bindQueue(Channel channel, RpcWorker worker) throws IOException;
    }

    private final WorkerFactory workerFactory;
    private final String queuePrefix;
    private final QueueDeclarator queueDeclarator;
    private final PostProcessors postProcessors;
    private final RabbitReconnector reconnector;

    public RpcServer(WorkerFactory workerFactory, String queuePrefix) {
        this(workerFactory, queuePrefix, null, null, null);
    }

    /**
     * @param workerFactory
     * @param queuePrefix
     * @param queueDeclarator
     * @param postProcessors
     * @param reconnectLogger
     */
    public RpcServer(WorkerFactory workerFactory, String queuePrefix, QueueDeclarator queueDeclarator, PostProcessors postProcessors, ReconnectLogger reconnectLogger) {
        this.workerFactory = workerFactory;
        this.queuePrefix = queuePrefix;
        this.queueDeclarator = queueDeclarator;
        this.postProcessors = postProcessors;

        ReconnectHandler reconnectHandler = new ReconnectHandler() {
            @Override
            public boolean reconnect() throws Exception {
                start();
                return true;
            }
        };

        reconnector = new RabbitReconnector(reconnectHandler, reconnectLogger, 1);
    }

    /**
     * 1. Creates {@link RpcWorker}s using the {@link WorkerFactory} provided to the constructor.
     * 2. Declares queues & binds routing-keys if a (@link QueueDeclarator} was provided to the constructor.
     * 3. Starts each {@link RpcWorker}.
     *
     * @throws IOException
     */
    public void start() throws IOException {
        RpcWorkers workers = workerFactory.createWorkers(queuePrefix);

        Connection conn = workers.getConnection();
        conn.addShutdownListener(reconnector);
        Channel channel = conn.createChannel();

        if (queueDeclarator != null) {
            for (RpcWorker worker : workers) {
                queueDeclarator.declareQueue(channel, worker);
                queueDeclarator.bindQueue(channel, worker);
            }
        }

        for (RpcWorker worker : workers) {
            worker.setPostProcessors(postProcessors);
            String queueName = worker.getQueueName();

            System.out.println(channel + " - Starting Worker for queue: " + queueName);
            channel.queueDeclarePassive(queueName); // make sure the handler's queue exists
            worker.start();
        }
    }
}
