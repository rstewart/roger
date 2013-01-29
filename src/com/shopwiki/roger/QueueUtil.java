package com.shopwiki.roger;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Maps;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;

/**
 * @owner rstewart
 */
public class QueueUtil {
    // TODO: Make these non-static and make args an instance-field ???

    /* Can't instantiate. Just static methods. */ 
    private QueueUtil() { }

    /**
     * Used by RpcClients.
     * Same as ChannelN.queueDeclare()
     */
    public static DeclareOk declareAnonymousQueue(Channel channel, Map<String,Object> args) throws IOException {
        final String queueName = "";
        final boolean exclusive = true;
        return declareTempQueue(channel, queueName, exclusive, args);
    }

    /**
     * Used by temporary (i.e. test) RpcServers (exclusive = true)
     */
    public static DeclareOk declareTempQueue(Channel channel, String queueName, boolean exclusive, Map<String,Object> args) throws IOException {
        final boolean durable = false;
        final boolean autoDelete = true;
        return declareQueue(channel, queueName, durable, exclusive, autoDelete, args);
    }

    /**
     * Used by permanent (i.e. production) RpcServers
     */
    public static DeclareOk declareNamedQueue(Channel channel, String queueName, Map<String,Object> args) throws IOException {
        final boolean durable = true; // TODO: make configurable ???
        final boolean exclusive = false;
        final boolean autoDelete = false;
        return declareQueue(channel, queueName, durable, exclusive, autoDelete, args);
    }

    private static DeclareOk declareQueue(Channel channel, String queueName, boolean durable, boolean exclusive, boolean autoDelete, Map<String,Object> args) throws IOException {
        if (args == null) {
            args = Maps.newHashMap();
        }
        return channel.queueDeclare(queueName, durable, exclusive, autoDelete, args);
    }
}
