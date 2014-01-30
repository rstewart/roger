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

import org.codehaus.jackson.type.TypeReference;

import com.google.common.util.concurrent.AbstractFuture;
import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.shopwiki.roger.*;

/**
 * Mainly for testing {@link RpcServer}s.
 *
 * @param <O> response type
 *
 * @author rstewart
 */
public class RpcClient<O> {

    private static final boolean DEBUG = MessagingUtil.DEBUG;

    private final Channel channel;
    private final Route requestRoute;
    private final TypeReference<O> responseType;
    private final boolean exceptionsAsJson;

    private final String responseQueueName;

    // TODO: Make this a Guava Cache with TTLs on the entries so we don't leak Futures that never complete ???
    // Or better yet... make it a Map of WeakReferences, and have a background thread delete keys that no longer have values ???
    // Or best... use Guava CacheBuilder ???
    private final Map<String,RpcFuture<O>> idToFuture = new ConcurrentHashMap<String,RpcFuture<O>>();

    /**
     * @param channel
     * @param requestRoute
     * @param responseQueueArgs
     * @param exceptionAsJson
     * @return RpcClient that returns all responses as MapMessages.
     * @throws IOException
     */
    public static RpcClient<MapMessage> create(
            Channel channel,
            Route requestRoute,
            Map<String,Object> responseQueueArgs,
            boolean exceptionAsJson
            ) throws IOException {

        return new RpcClient<MapMessage>(channel, requestRoute, responseQueueArgs, MapMessage.TYPE_REF, exceptionAsJson);
    }

    /**
     * @param channel
     * @param requestRoute
     * @param responseQueueArgs
     * @param responseType
     * @return RpcClient that returns responses using the supplied responseType.
     * @throws IOException
     */
    public static <O> RpcClient<O> create(
            Channel channel,
            Route requestRoute,
            Map<String,Object> responseQueueArgs,
            TypeReference<O> responseType
            ) throws IOException {

        return new RpcClient<O>(channel, requestRoute, responseQueueArgs, responseType, false);
    }

    private RpcClient(
            Channel channel,
            Route requestRoute,
            Map<String,Object> responseQueueArgs,
            TypeReference<O> responseType,
            boolean exceptionsAsJson
            ) throws IOException {

        this.channel = channel;
        this.requestRoute = requestRoute;
        this.responseType = responseType;
        this.exceptionsAsJson = exceptionsAsJson;

        if (exceptionsAsJson && ! responseType.getType().equals(MapMessage.TYPE_REF.getType())) {
            throw new IllegalArgumentException("Can't have exceptionsAsJson unless your responseType is MapMessage!");
        }

        responseQueueName = QueueUtil.declareAnonymousQueue(channel, "Roger-RpcClient", responseQueueArgs).getQueue();
        Consumer responseConsumer = new ResponseConsumer(channel);
        channel.basicConsume(responseQueueName, true, responseConsumer);
    }

    private class ResponseConsumer extends DefaultConsumer {

        public ResponseConsumer(Channel channel) {
            super(channel);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties props, byte[] body) throws IOException {
            // AUTO-ACKING

            RpcFuture<O> future = idToFuture.remove(props.getCorrelationId());
            if (future == null) {
                System.err.println("### Received a response not meant for me! ###");
                System.err.println("### " + MessagingUtil.prettyPrint(props));
                System.err.println("### " + MessagingUtil.prettyPrintMessage(body));
                return;
            }

            ResponseStatus status = RpcResponse.getStatus(props);

            final Object debugMessage;

            if (status == ResponseStatus.OK || exceptionsAsJson) {
                O message = MessagingUtil.getDeliveryBody(body, responseType);
                debugMessage = message;
                RpcResponse<O> response = new RpcResponse<O>(props, message);
                future.complete(response);
            } else {
                MapMessage message = MessagingUtil.getDeliveryBody(body, MapMessage.TYPE_REF);
                debugMessage = message;
                future.completeException(status, message);
            }

            if (DEBUG) {
                System.out.println("*** ResponseConsumer RECEIVED RESPONSE ***");
                System.out.println("*** consumerTag: " + consumerTag);
                System.out.println("*** envelope:\n" + MessagingUtil.prettyPrint(envelope));
                System.out.println("*** properties:\n" + MessagingUtil.prettyPrint(props));
                System.out.println("*** response: " + MessagingUtil.prettyPrintMessage(debugMessage));
            }
        }
    }

    /**
     * @param request
     * @return response
     * @throws IOException
     */
    public Future<RpcResponse<O>> sendRequest(Object request) throws IOException {
        String id = UUID.randomUUID().toString();
        RpcFuture<O> future = new RpcFuture<O>();
        idToFuture.put(id, future);
        MessagingUtil.sendRequest(channel, requestRoute, request, responseQueueName, id);
        return future;
    }

    private static class RpcFuture<T> extends AbstractFuture<RpcResponse<T>> {
        void complete(RpcResponse<T> response) {
            set(response);
        }

        void completeException(ResponseStatus status, MapMessage exceptionResponse) {
            String exceptionName = (String)exceptionResponse.get("exceptionName");
            String exceptionMsg  = (String)exceptionResponse.get("exceptionMsg");
            Exception e = new Exception(status + ": " + exceptionName + "\n" + exceptionMsg);
            setException(e);
        }
    }
}
