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

package com.shopwiki.messaging;

import com.rabbitmq.client.*;
import com.shopwiki.messaging.util.TimeUtil;

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
        System.err.println(TimeUtil.now() + " RabbitMQ connection SHUTDOWN!");
        System.err.print("CAUSE: ");
        cause.printStackTrace();
        run();
    }

    @Override
    public void run() {
        System.err.println(TimeUtil.now() + " Attempting to reconnect to RabbitMQ...");
        int attempt = 0;

        while (true) {
            attempt++;
            try {
                if (handler.reconnect()) {
                    System.err.println(TimeUtil.now() + " RabbitMQ reconnect # " + attempt + " SUCCEEDED!");
                    return;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

            if (logger != null) {
                logger.log(attempt);
            }
            System.err.println(TimeUtil.now() + " RabbitMQ reconnect # " + attempt + " FAILED!  Retrying in " + secondsBeforeRetry + " seconds...");
            try {
                Thread.sleep(secondsBeforeRetry * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
