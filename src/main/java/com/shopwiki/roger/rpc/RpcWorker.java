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

import com.google.common.collect.*;
import com.rabbitmq.client.Channel;

/**
 * A glue class that packages together a {@link RequestHandler} and the RabbitMQ plumbing needed to use it.
 * The user is expected to instantiate these in a {@link RpcServer.WorkerFactory}.
 * However, it is also common to just use the {@link BasicWorkerFactory} implementation.
 *
 * @author rstewart
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
