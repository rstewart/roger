package com.shopwiki.messaging.rpc;

import java.io.IOException;

import com.rabbitmq.client.*;
import com.shopwiki.messaging.MessagingReconnector;
import com.shopwiki.messaging.MessagingReconnector.*;

/**
 * @owner rstewart
 */
public class RpcServer {

    public interface WorkerFactory {
        RpcWorkers createWorkers(String queuePrefix) throws IOException;
    }

    public static interface QueueDeclarator {
        void declareQueue(Channel channel, RpcWorker worker) throws IOException;
        void bindQueue(Channel channel, RpcWorker worker) throws IOException;
    }

    private final WorkerFactory workerFactory;
    private final String queuePrefix;
    private final QueueDeclarator queueDeclarator;
    private final PostProcessors postProcessors;
    private final MessagingReconnector reconnector;

    public RpcServer(WorkerFactory workerFactory, String queuePrefix) {
        this(workerFactory, queuePrefix, null, null, null);
    }

    public RpcServer(WorkerFactory workerFactory, String queuePrefix, QueueDeclarator queueDeclarator, PostProcessors postProcessors, ReconnectLogger reconnectLogger) {
        this.workerFactory = workerFactory;
        this.queuePrefix = queuePrefix;
        this.queueDeclarator = queueDeclarator;
        this.postProcessors = postProcessors;

        ReconnectHandler reconnectHandler = new ReconnectHandler() {
            @Override
            public boolean reconnect() throws Exception {
                init();
                return true;
            }
        };

        reconnector = new MessagingReconnector(reconnectHandler, reconnectLogger, 1);
    }

    private void init() throws IOException {
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

    public void start() throws IOException {
        init();
    }
}