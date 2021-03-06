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
import java.util.List;
import java.util.concurrent.Executors;

import com.rabbitmq.client.*;
import com.shopwiki.roger.RabbitConnector;
import com.shopwiki.roger.RabbitReconnector;
import com.shopwiki.roger.RabbitReconnector.*;
import com.shopwiki.roger.util.TimeUtil;

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
        Connection createConnection() throws IOException;
        List<RpcWorker> createWorkers(Connection conn, String queuePrefix) throws IOException;
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
    public final String queuePrefix;
    private final QueueDeclarator queueDeclarator;
    private final PostProcessors postProcessors;
    private final RabbitReconnector reconnector;

    /**
     * @param workerFactory
     * @param queuePrefix
     */
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
                return _start();
            }
        };

        int secondsBeforeReconnect = 1; // TODO: Make this configurable ???
        reconnector = new RabbitReconnector(reconnectHandler, reconnectLogger, secondsBeforeReconnect);
    }

    /**
     * Creates {@link RpcWorker}s using the {@link WorkerFactory} provided to the constructor.
     * Declares queues & binds routing-keys if a {@link QueueDeclarator} was provided to the constructor.
     * Starts each {@link RpcWorker}.
     *
     * If this fails for any reason, it will periodically attempt to start in a background thread.
     */
    public void start() {
        if (_start() == false) {
            Executors.newSingleThreadExecutor().execute(reconnector);
        }
    }

    private volatile Connection conn = null;

    private boolean _start() {
        try {
            System.out.print(TimeUtil.now() + " Starting RpcServer: ");
            conn = workerFactory.createConnection();
            List<RpcWorker> workers = workerFactory.createWorkers(conn, queuePrefix);
            System.out.println(conn + " with " + workers.size() + " workers.");

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

                System.out.println("\t" + "Starting RpcWorker for queue: " + queueName);
                channel.queueDeclarePassive(queueName); // make sure the handler's queue exists
                worker.start();
            }

            channel.close();
            conn.addShutdownListener(reconnector);
            return true;
        } catch (Throwable t) {
            System.err.println(TimeUtil.now() + " ERROR starting RpcServer:");
            t.printStackTrace();
            stop();
            return false;
        }
    }

    /**
     * Closes the connection to RabbitMQ and prevents further automatic restarts.
     */
    public void stop() {
        System.out.println(TimeUtil.now() + " Stopping RpcServer: " + conn);
        RabbitConnector.closeConnectionAndRemoveReconnector(conn, reconnector);
        conn = null;
    }
}
