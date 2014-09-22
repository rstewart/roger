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
    
    private final String username;
    
    private final String password;

    public RabbitConnector(Address address) {
        this(address, "guest", "guest");
    }
    
    public RabbitConnector(Address address, String username, String password) {
        this(Arrays.asList(address), username, password);
    }

    public RabbitConnector(List<Address> addresses) {
        this(addresses, "guest", "guest");
    }
    
    public RabbitConnector(List<Address> addresses, String username, String password) {
        this.addresses = ImmutableList.copyOf(addresses);
        this.username = username;
        this.password = password;
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
                conn = _newConnection(numThreads);
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
    public Connection newDaemonConnection(final int numThreads) throws IOException {
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
    public Connection newConnection(int numThreads) throws IOException {
        Connection conn = _newConnection(numThreads);
        if (DEBUG) { System.out.println("*** Connected to RabbitMQ: " + conn); }
        return conn;
    }

    private Connection _newConnection(int numThreads) throws IOException {
        ConnectionFactory connFactory = new ConnectionFactory();
        connFactory.setUsername(this.username);
        connFactory.setPassword(this.password);
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

    /**
     * Close connection if it's not null.
     * Swallow IOExceptions.
     * @param conn
     */
    public static void closeConnection(Connection conn) {
        closeConnectionAndRemoveReconnector(conn, null);
    }

    /**
     * Remove RabbitReconnector if it's not null (to prevent reconnect).
     * Close connection if it's not null.
     * Swallow IOExceptions.
     * @param conn
     * @param reconnector
     */
    public static void closeConnectionAndRemoveReconnector(Connection conn, RabbitReconnector reconnector) {
        if (conn == null) {
            return;
        }

        if (reconnector != null) {
            conn.removeShutdownListener(reconnector);
        }

        try {
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
            //throw new RuntimeException(e);
        }
    }
}
