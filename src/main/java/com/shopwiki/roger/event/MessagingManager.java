/*
 * Copyright [2013] [ShopWiki]
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.shopwiki.roger.RabbitConnector;
import com.shopwiki.roger.RabbitReconnector;
import com.shopwiki.roger.Route;
import com.shopwiki.roger.RabbitReconnector.ReconnectHandler;
import com.shopwiki.roger.RabbitReconnector.ReconnectLogger;
import com.shopwiki.roger.util.TimeUtil;

/**
 * The main entry point for creating & starting message workers using Roger.
 *
 * @author rstewart
 */
public class MessagingManager {

    private final RabbitConnector connector;
    private final int numThreads;
    private final RabbitReconnector reconnector;
    private final Map<String, Object> queueArgs = Collections.emptyMap(); // TODO: Pass in queueArgs ???

    private final Map<MessageHandler<?>, Route> handlerToRoute = new ConcurrentHashMap<MessageHandler<?>, Route>();
    private volatile Connection conn = null;
    private volatile List<Channel> channels = null;

    public MessagingManager(RabbitConnector connector, int numThreads, int secondsBeforeReconnect) {
        this(connector, numThreads, secondsBeforeReconnect, null);
    }

    public MessagingManager(RabbitConnector connector, int numThreads, int secondsBeforeReconnect, ReconnectLogger reconnectLogger) {
        this.connector = connector;
        this.numThreads = numThreads;

        ReconnectHandler reconnectHandler = new ReconnectHandler() {
            @Override
            public boolean reconnect() throws Exception {
                return _start();
            }
        };

        reconnector = new RabbitReconnector(reconnectHandler, reconnectLogger, secondsBeforeReconnect);
    }

    public synchronized void add(MessageHandler<?> handler, Route route) throws IOException {
        handlerToRoute.put(handler, route);
        if (channels != null) {
            startWorker(handler, route);
        }
    }

    private void startWorker(MessageHandler<?> handler, Route route) throws IOException {
        MessageWorker worker = new MessageWorker(handler, channels, queueArgs, route);
        System.out.println("\t" + "Starting MessageWorker for route: " + route);
        worker.start();
    }

    public void start() {
        if (_start() == false) {
            Executors.newSingleThreadExecutor().execute(reconnector);
        }
    }

    private synchronized boolean _start() {
        try {
            System.out.print(TimeUtil.now() + " Starting MessagingManager: ");
            conn = connector.getDaemonConnection(numThreads); // TODO: Should have the option for non-daemon ???
            channels = createChannels(conn, numThreads);
            System.out.println(conn + " with " + channels.size() + " channels.");

            for (MessageHandler<?> handler : handlerToRoute.keySet()) {
                Route route = handlerToRoute.get(handler);
                startWorker(handler, route);
            }

            conn.addShutdownListener(reconnector);
            return true;
        } catch (Throwable t) {
            System.err.println(TimeUtil.now() + " ERROR starting MessagingManager:");
            t.printStackTrace();
            channels = null;
            stop();
            return false;
        }
    }

    private static List<Channel> createChannels(Connection conn, int n) throws IOException {
        List<Channel> channels = Lists.newArrayList();
        for (int i = 0; i < n; i++) {
            Channel channel = conn.createChannel();
            channels.add(channel);
        }
        return ImmutableList.copyOf(channels);
    }

    public void stop() {
        System.out.println(TimeUtil.now() + " Stopping MessagingManager: " + conn);
        RabbitConnector.closeConnectionAndRemoveReconnector(conn, reconnector);
        conn = null;
    }
}
