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
import java.net.InetAddress;
import java.util.*;

import org.codehaus.jackson.type.TypeReference;

import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.shopwiki.roger.MessagingUtil;

/**
 * Implementation of RabbitMQ's Consumer interface.
 * The user is not expected to use this class directly.
 * See {@link RpcWorker} instead.
 *
 * @author rstewart
 *
 * @param <I> request type (I is for input).
 */
public class RequestConsumer<I> extends DefaultConsumer {

    private static final boolean DEBUG = MessagingUtil.DEBUG;

    private static final String hostname;
    static {
        String s = null;
        try {
            s = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            // do nothing
        }
        hostname = s;
    }

    private final RequestHandler<I,?> handler;
    private final TypeReference<I> requestType;
    private final Channel channel;
    private final String queueName;
    private final PostProcessors pps;

    public static <I> void start(RequestHandler<I,?> handler, List<Channel> channels, String queueName, PostProcessors postProcessors) throws IOException {
        for (Channel channel : channels) {
            Consumer consumer = new RequestConsumer<I>(handler, channel, queueName, postProcessors);
            channel.basicConsume(queueName, false, consumer);
        }
    }

    private RequestConsumer(RequestHandler<I,?> handler, Channel channel, String queueName, PostProcessors pps) {
        super(channel);
        this.handler = handler;
        this.requestType = handler.getRequestType();
        this.channel = channel;
        this.queueName = queueName;
        this.pps = pps;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties requestProps, byte[] body) throws IOException {

        I request = null;
        ResponseStatus status = null;
        Object response = null;
        Throwable thrown = null;
        long timeTaken = -1;

        try {
            long startTime = System.currentTimeMillis();

            try {
                request = MessagingUtil.getDeliveryBody(body, requestType);
            } catch (Exception e) {
                response = new ExceptionResponse(e);
                status = ResponseStatus.MALFORMED_REQUEST;
                thrown = e;
            }

            if (DEBUG) {
                System.out.println("*** RequestHandler " + handler.getClass().getCanonicalName() + " RECEIVED REQUEST ***");
                System.out.println("*** consumerTag: " + consumerTag);
                System.out.println("*** envelope:\n" + MessagingUtil.prettyPrint(envelope));
                System.out.println("*** requestProps:\n" + MessagingUtil.prettyPrint(requestProps));
                System.out.println("*** request: " + MessagingUtil.prettyPrintMessage(request));
                System.out.println();
            }

            if (request != null) {
                try {
                    response = handler.handleRequest(request);
                    status = ResponseStatus.OK;
                } catch (NackException e) {
                    thrown = e;
                    channel.basicNack(envelope.getDeliveryTag(), false, true);
                    return;
                } catch (IllegalArgumentException e) {
                    response = new ExceptionResponse(e);
                    status = ResponseStatus.INVALID_REQUEST;
                    thrown = e;
                } catch (Throwable e) {
                    response = new ExceptionResponse(e);
                    status = ResponseStatus.HANDLER_ERROR;
                    thrown = e;
                }
            }

            timeTaken = System.currentTimeMillis() - startTime;

            // Ack before or after the reply is sent ???
            channel.basicAck(envelope.getDeliveryTag(), false);

            String replyTo = requestProps.getReplyTo();
            String correlationId = requestProps.getCorrelationId();

            if (correlationId != null && replyTo != null) {
                Map<String,Object> headers = new LinkedHashMap<String,Object>();
                headers.put("status", String.valueOf(status));
                headers.put("handler_time_millis", String.valueOf(timeTaken));
                headers.put("hostname", hostname);

                BasicProperties.Builder replyProps = new BasicProperties.Builder();
                replyProps = replyProps.headers(headers);
                replyProps = replyProps.correlationId(correlationId);
                // TODO: delivery mode ???
                MessagingUtil.sendResponse(channel, replyTo, response, replyProps);
            }
        } catch (Throwable e) {
            status = ResponseStatus.NUCLEAR;
            thrown = e;
        } finally {
            if (pps != null) {
                pps.process(handler, queueName, status, request, response, thrown, timeTaken);
            }
        }
    }
}
