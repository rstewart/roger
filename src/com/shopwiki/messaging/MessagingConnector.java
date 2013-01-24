package com.shopwiki.messaging;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import com.google.common.collect.ImmutableList;
import com.rabbitmq.client.*;
import com.shopwiki.messaging.util.DaemonThreadFactory;

/**
 * @owner rstewart
 */
public class MessagingConnector {

    private final List<Address> addresses;

    public MessagingConnector(Address address) {
        this(Arrays.asList(address));
    }

    public MessagingConnector(List<Address> addresses) {
        this.addresses = ImmutableList.copyOf(addresses);
    }

    private class ConnectHackDaemon extends Thread {
        private final int numThreads;

        public ConnectHackDaemon(int numThreads) {
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
            setDaemon( true );
            super.start();
        }
    }

    public Connection getConnection(final int numThreads) throws IOException {
        ConnectHackDaemon tempThread = new ConnectHackDaemon(numThreads);
        tempThread.start();
        try {
            tempThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (tempThread.ioe != null) {
            throw tempThread.ioe;
        }

        if (MessagingUtil.DEBUG) { System.out.println("*** Connected to RabbitMQ: " + tempThread.conn); }
        return tempThread.conn;
    }

    public Connection getLongConnection(int numThreads) throws IOException {
        Connection conn = _getConnection(numThreads);
        if (MessagingUtil.DEBUG) { System.out.println("*** Connected to RabbitMQ: " + conn); }
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
