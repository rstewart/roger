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
import java.util.*;
import java.util.concurrent.*;

import com.google.common.util.concurrent.AbstractFuture;
import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.shopwiki.roger.*;

/**
 * Mainly for testing a {@link RpcServer}s.
 *
 * @author rstewart
 */
public class RpcClient {

    private final Channel channel;
    private final Route requestRoute;
    private final boolean exceptionsAsJson;

    private final String responseQueueName;

    private final Map<String,RpcFuture> idToFuture = new ConcurrentHashMap<String,RpcFuture>();

    public RpcClient(Channel channel, Route requestRoute, Map<String,Object> responseQueueArgs, boolean exceptionsAsJson) throws IOException {
        this.channel = channel;
        this.requestRoute = requestRoute;
        this.exceptionsAsJson = exceptionsAsJson;

        responseQueueName = QueueUtil.declareAnonymousQueue(channel, responseQueueArgs).getQueue();
        Consumer responseConsumer = new ResponseConsumer(channel);
        channel.basicConsume(responseQueueName, true, responseConsumer);
    }

    private class ResponseConsumer extends DefaultConsumer {

        public ResponseConsumer(Channel channel) {
            super(channel);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties props, byte[] body) throws IOException {

            MapMessage message = MessagingUtil.getDeliveryBody(body, MapMessage.TYPE_REF);

            if (MessagingUtil.DEBUG) {
                System.out.println("*** ResponseConsumer RECEIVED RESPONSE ***");
                System.out.println("*** consumerTag: " + consumerTag);
                System.out.println("*** envelope:\n" + MessagingUtil.prettyPrint(envelope));
                System.out.println("*** properties:\n" + MessagingUtil.prettyPrint(props));
                System.out.println("*** response: " + MessagingUtil.prettyPrintMessage(message));
            }

            RpcFuture future = idToFuture.remove(props.getCorrelationId());
            if (future == null) {
                System.err.println("### Received a response not meant for me! ###");
                System.err.println("### " + MessagingUtil.prettyPrint(props));
                System.err.println("### " + MessagingUtil.prettyPrintMessage(body));
                return;
            }

            RpcResponse response = new RpcResponse(props, message);

            if (exceptionsAsJson) {
                future.complete(response); // return JSON regardless of the status
            } else {
                future.completeExcept(response);
            }
        }
    }

    /**
     * @param request
     * @return response
     * @throws IOException Only if exceptionsAsJson is set to true.
     */
    public Future<RpcResponse> sendRequest(Object request) throws IOException {
        String id = java.util.UUID.randomUUID().toString();
        MessagingUtil.sendRequest(channel, requestRoute, request, responseQueueName, id);

        RpcFuture future = new RpcFuture();
        idToFuture.put(id, future);
        return future;
    }

    private static class RpcFuture extends AbstractFuture<RpcResponse> {
        private void complete(RpcResponse response) {
            set(response);
        }

        private void completeExcept(RpcResponse response) {
            ResponseStatus status = response.getStatus();
            if (status == ResponseStatus.OK) {
                set(response);
            } else {
                MapMessage body = response.getBody();
                String exceptionName = (String)body.get("exceptionName");
                String exceptionMsg  = (String)body.get("exceptionMsg");
                Exception e = new Exception(status + ": " + exceptionName + "\n" + exceptionMsg);
                setException(e);
            }
        }
    }
}
