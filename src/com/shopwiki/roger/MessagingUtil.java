package com.shopwiki.roger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.map.*;
import org.codehaus.jackson.type.TypeReference;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;

/**
 * @owner rstewart
 */
public class MessagingUtil {

    public static final boolean DEBUG = Boolean.getBoolean("DEBUG.MESSAGING");

    private static final Charset UTF_8 = Charset.forName("utf-8");

    /* Same as ChannelN.queueDeclare() */
    public static DeclareOk declareAnonymousQueue(Channel channel) throws IOException {
        final String queueName = "";
        final boolean exclusive = true;
        return declareTempQueue(channel, queueName, exclusive);
    }

    public static DeclareOk declareTempQueue(Channel channel, String queueName, boolean exclusive) throws IOException {
        final boolean durable = false;
        final boolean autoDelete = true;
        Map<String,Object> args = new HashMap<String,Object>();
        return declareQueue(channel, queueName, durable, exclusive, autoDelete, args);
    }

    public static DeclareOk declareNamedQueue(Channel channel, String queueName) throws IOException {
        final boolean durable = true;
        final boolean exclusive = false;
        final boolean autoDelete = false;
        Map<String,Object> args = new HashMap<String,Object>();
        args.put("x-ha-policy", "all"); // replicate the queue to all slaves
        return declareQueue(channel, queueName, durable, exclusive, autoDelete, args);
    }

    private static DeclareOk declareQueue(Channel channel, String queueName, boolean durable, boolean exclusive, boolean autoDelete, Map<String,Object> args) throws IOException {
        // Set the message TTL for all queues created
        // http://www.rabbitmq.com/extensions.html#lifetimes
        args.put("x-message-ttl", TimeUnit.HOURS.toSeconds(1));
        return channel.queueDeclare(queueName, durable, exclusive, autoDelete, args);
    }

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
            System.out.println("*** RabbitMQUtil SENDING MESSAGE ***");
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
