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
import com.shopwiki.roger.event.MessageConsumer;
import com.shopwiki.roger.event.MessageHandler;

/**
 * Mainly for testing a {@link RpcServer}s.
 *
 * @author rstewart
 */
public class RpcClient {

    private final Channel channel;
    private final Route requestRoute;
    private final boolean exceptionsAsJson;

    private final MessageConsumer<MapMessage> responseConsumer;

    private final Map<String,RpcFuture> idToFuture = new ConcurrentHashMap<String,RpcFuture>();

    public RpcClient(Channel channel, Route requestRoute, Map<String,Object> responseQueueArgs, boolean exceptionsAsJson) throws IOException {
        this.channel = channel;
        this.requestRoute = requestRoute;
        this.exceptionsAsJson = exceptionsAsJson;

        ResponseHandler handler = new ResponseHandler();
        responseConsumer = new MessageConsumer<MapMessage>(handler, channel, responseQueueArgs, null);
        responseConsumer.start();
    }

    private class ResponseHandler implements MessageHandler<MapMessage> {

        @Override
        public TypeReference<MapMessage> getMessageType() {
            return MapMessage.TYPE_REF; // TODO: Make generic (NOT MapMessage) ???
        }

        @Override
        public void handleMessage(MapMessage body, BasicProperties props) {
            RpcFuture future = idToFuture.remove(props.getCorrelationId());
            if (future == null) {
                System.err.println("### Received a response not meant for me! ###");
                System.err.println("### " + MessagingUtil.prettyPrint(props));
                System.err.println("### " + MessagingUtil.prettyPrintMessage(body));
                return;
            }

            RpcResponse response = new RpcResponse(props, body);

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
        String replyQueue = responseConsumer.getQueueName();
        MessagingUtil.sendRequest(channel, requestRoute, request, replyQueue, id);

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
