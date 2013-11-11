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

package com.shopwiki.roger.event;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.rabbitmq.client.*;
import com.shopwiki.roger.MessagingUtil;
import com.shopwiki.roger.Route;

/**
 * A glue class that packages together a {@link MessageHandler} and the RabbitMQ plumbing needed to use it.
 * The user is expected to instantiate one of these and call the {@link #start()} method to begin handling messages.
 *
 * @author rstewart
 */
public class MessageWorker { // TODO: This class isn't really necessary since MessagingManager doesn't use a WorkerFactory

    private final MessageHandler<?> handler;
    private final List<Channel> channels;
    private final Map<String,Object> queueArgs;
    private final Route route;

    /**
     * @param connector
     * @param handler
     * @param queueArgs
     * @param route
     * @param reconnectLogger
     * @param daemon
     */
    public MessageWorker(
            MessageHandler<?> handler,
            List<Channel> channels,
            Map<String,Object> queueArgs,
            Route route
            ) {

        this.handler = handler;
        this.channels = ImmutableList.copyOf(channels);
        this.queueArgs = queueArgs;
        this.route = route;
    }

    /**
     * Call this to start consuming & handling messages.
     */
    public void start() throws IOException {
        MessageConsumer.start(handler, channels, queueArgs, route);
    }

    // TODO: Get rid of this ???
    /**
     * Send a message to the same route this Worker is receiving from.
     * @param message
     * @throws IOException
     */
    public void sendMessage(Object message) throws IOException {
        Channel channel = channels.get(0);
        MessagingUtil.sendMessage(channel, route, message);
    }
}
