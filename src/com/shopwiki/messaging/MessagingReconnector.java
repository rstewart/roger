package com.shopwiki.messaging;

import com.rabbitmq.client.*;

/**
 * @owner rstewart
 */
public class MessagingReconnector implements ShutdownListener, Runnable {

    public static interface ReconnectHandler {
        boolean reconnect() throws Exception;
    }

    public static interface ReconnectLogger {
        void log(ShutdownSignalException cause);
        void log(int attempt);
    }

    private final ReconnectHandler handler;
    private final ReconnectLogger logger;
    private final int secondsBeforeRetry;

    public MessagingReconnector(ReconnectHandler handler, int secondsBeforeRetry) {
        this(handler, null, secondsBeforeRetry);
    }

    public MessagingReconnector(ReconnectHandler handler, ReconnectLogger logger, int secondsBeforeRetry) {
        this.handler = handler;
        this.logger = logger;
        this.secondsBeforeRetry = secondsBeforeRetry;
    }

    @Override
    public void shutdownCompleted(ShutdownSignalException cause) {
        System.err.println("RabbitMQ connection SHUTDOWN!");
        System.err.print("CAUSE: ");
        cause.printStackTrace();
        run();
    }

    @Override
    public void run() {
        System.err.println("Attempting to reconnect to RabbitMQ...");
        int attempt = 0;

        while (true) {
            attempt++;
            try {
                if (handler.reconnect()) {
                    System.err.println("RabbitMQ reconnect # " + attempt + " SUCCEEDED!");
                    return;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

            if (logger != null) {
                logger.log(attempt);
            }
            System.err.println("RabbitMQ reconnect # " + attempt + " FAILED!  Retrying in " + secondsBeforeRetry + " seconds...");
            try {
                Thread.sleep(secondsBeforeRetry * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
