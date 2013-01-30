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

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import org.codehaus.jackson.map.*;
import org.codehaus.jackson.type.TypeReference;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.*;

/**
 * Static methods for sending JSON-formatted messages (using <A href="http://jackson.codehaus.org/">Jackson</A>) over RabbitMQ.
 *
 * @author rstewart
 */
public class MessagingUtil {

    public static final boolean DEBUG = Boolean.getBoolean("ROGER.DEBUG");

    private static final Charset UTF_8 = Charset.forName("utf-8");

    /* Can't instantiate. Just static methods. */
    private MessagingUtil() { }

    public static void sendMessage(Channel channel, Route route, Object message) throws IOException {
        BasicProperties.Builder props = new BasicProperties.Builder();
        sendMessage(channel, route.exchange, route.key, message, props);
    }

    public static void sendRequest(Channel channel, Route route, Object request, String callbackQueue, String correlationId) throws IOException {
        BasicProperties.Builder props = new BasicProperties.Builder().replyTo(callbackQueue).correlationId(correlationId);
        sendMessage(channel, route.exchange, route.key, request, props);
    }

    public static void sendResponse(Channel channel, String queueName, Object response, BasicProperties.Builder props) throws IOException {
        String exchange = "";
        sendMessage(channel, exchange, queueName, response, props);
    }

    private static void sendMessage(Channel channel, String exchange, String routingKey, Object message, BasicProperties.Builder props) throws IOException {
        byte[] bytes = objectMapper.writeValueAsBytes(message);
        props = props.contentEncoding(UTF_8.name());
        props = props.contentType("application/json");
        props = props.timestamp(new Date());
        if (DEBUG) {
            System.out.println("*** MessagingUtil SENDING MESSAGE ***");
            System.out.println("*** routingKey: " + routingKey);
            System.out.println("*** props:\n" + prettyPrint(props.build()));
            System.out.println("*** message: " + prettyPrintMessage(message));
            System.out.println();
        }
        channel.basicPublish(exchange, routingKey, props.build(), bytes);
    }

    public static String prettyPrint(BasicProperties props) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t" + "ContentType: "     + props.getContentType()     + "\n");
        sb.append("\t" + "ContentEncoding: " + props.getContentEncoding() + "\n");
        sb.append("\t" + "Headers: "         + props.getHeaders()         + "\n");
        sb.append("\t" + "DeliveryMode: "    + props.getDeliveryMode()    + "\n");
        sb.append("\t" + "Priority: "        + props.getPriority()        + "\n");
        sb.append("\t" + "CorrelationId: "   + props.getCorrelationId()   + "\n");
        sb.append("\t" + "ReplyTo: "         + props.getReplyTo()         + "\n");
        sb.append("\t" + "Expiration: "      + props.getExpiration()      + "\n");
        sb.append("\t" + "MessageId: "       + props.getMessageId()       + "\n");
        sb.append("\t" + "Timestamp: "       + props.getTimestamp()       + "\n");
        sb.append("\t" + "Type: "            + props.getType()            + "\n");
        sb.append("\t" + "UserId: "          + props.getUserId()          + "\n");
        sb.append("\t" + "AppId: "           + props.getAppId()                 );
        return sb.toString();
    }

    public static String prettyPrint(Envelope envelope) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t" + "Exchange: "    + envelope.getExchange()    + "\n");
        sb.append("\t" + "RoutingKey: "  + envelope.getRoutingKey()  + "\n");
        sb.append("\t" + "DeliveryTag: " + envelope.getDeliveryTag() + "\n");
        sb.append("\t" + "isRedeliver: " + envelope.isRedeliver()          );
        return sb.toString();
    }

    public static String prettyPrintMessage(Object message) {
        try {
            return prettyPrintWriter.writeValueAsString(message);
        } catch (Throwable e) {
            e.printStackTrace();
            return "CAN'T PRETTY PRINT MESSAGE!";
        }
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ObjectWriter prettyPrintWriter = objectMapper.defaultPrettyPrintingWriter();

    public static <T> T getDeliveryBody(byte[] body, TypeReference<?> typeRef) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(body);
        InputStreamReader reader = new InputStreamReader(in, UTF_8);
        return objectMapper.readValue(reader, typeRef);
    }
}
