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

package com.shopwiki.roger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import com.google.common.collect.ImmutableList;
import com.rabbitmq.client.*;
import com.shopwiki.roger.util.DaemonThreadFactory;

/**
 * Wraps a list of RabbitMQ server addresses.
 * Use an instance of this to create Connections.
 *
 * @author rstewart
 */
public class RabbitConnector { // I would have named this ConnectionFactory, but that's already taken by RabbitMQ.

    private static final boolean DEBUG = MessagingUtil.DEBUG;

    private final List<Address> addresses;

    public RabbitConnector(Address address) {
        this(Arrays.asList(address));
    }

    public RabbitConnector(List<Address> addresses) {
        this.addresses = ImmutableList.copyOf(addresses);
    }

    private class ConnectDaemon extends Thread {
        private final int numThreads;

        public ConnectDaemon(int numThreads) {
            this.numThreads = numThreads;
        }

        public volatile Connection conn = null;
        public volatile IOException ioe = null;

        @Override
        public void run() {
            try {
                conn = _getConnection(numThreads);
            } catch (IOException e) {
                ioe = e;
            }
        }

        @Override
        public synchronized void start() {
            setDaemon(true);
            super.start();
        }
    }

    /**
     * @param numThreads
     * @return A RabbitMQ Connection that uses daemon Threads (regardless of where it's called from).
     * @throws IOException
     */
    public Connection getDaemonConnection(final int numThreads) throws IOException {
        ConnectDaemon tempThread = new ConnectDaemon(numThreads);
        tempThread.start();
        try {
            tempThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (tempThread.ioe != null) {
            throw tempThread.ioe;
        }

        if (DEBUG) { System.out.println("*** Connected to RabbitMQ: " + tempThread.conn); }
        return tempThread.conn;
    }

    /**
     * @param numThreads
     * @return A RabbitMQ Connection whose Threads inherit their daemon-status from the calling Thread.
     * @throws IOException
     */
    public Connection getConnection(int numThreads) throws IOException {
        Connection conn = _getConnection(numThreads);
        if (DEBUG) { System.out.println("*** Connected to RabbitMQ: " + conn); }
        return conn;
    }

    private Connection _getConnection(int numThreads) throws IOException {
        ConnectionFactory connFactory = new ConnectionFactory();
        ThreadFactory threadFactory = DaemonThreadFactory.getInstance("RabbitMQ-ConsumerThread", true);
        final ExecutorService executor = Executors.newFixedThreadPool(numThreads, threadFactory);
        Address[] array = addresses.toArray(new Address[0]);
        Connection conn = connFactory.newConnection(executor, array);

        conn.addShutdownListener(new ShutdownListener() {
            @Override
            public void shutdownCompleted(ShutdownSignalException sse) {
                executor.shutdown();
            }
        });

        return conn;
    }

    // TODO: Move this to HessagingHelper ???
    public static void closeConnection(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            conn.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
