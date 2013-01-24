package com.shopwiki.messaging.rpc;

import java.io.IOException;
import java.util.*;

import com.rabbitmq.client.*;
import com.shopwiki.messaging.MessagingConnector;
import com.shopwiki.messaging.rpc.RpcServer.WorkerFactory;

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
