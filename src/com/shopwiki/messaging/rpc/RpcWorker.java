package com.shopwiki.messaging.rpc;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.*;
import com.rabbitmq.client.Channel;

/**
 * @owner rstewart
 */
public class RpcWorker {

    private final RequestHandler<?,?> handler;
    private final List<Channel> channels;
    private final String queueName;
    private final String procedureName;
    private PostProcessors pps;

    public RpcWorker(RequestHandler<?,?> handler, List<Channel> channels, String queuePrefix, String procedureName) {
        this.handler = handler;
        this.channels = ImmutableList.copyOf(channels);
        this.queueName = queuePrefix + procedureName;
        this.procedureName = procedureName;
    }

    public String getQueueName() {
        return queueName;
    }

    public String getProcedureName() {
        return procedureName;
    }

    public void setPostProcessors(PostProcessors postProcessors) {
        pps = postProcessors;
    }

    public void start() throws IOException {
        RequestConsumer.start(handler, channels, queueName, pps);
    }
}
