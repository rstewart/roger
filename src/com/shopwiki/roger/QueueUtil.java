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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;

/**
 * Static methods for declaring the 3 types of queues (used by Roger).
 *
 * @author rstewart
 */
public class QueueUtil {
    // TODO: Make methods non-static and make args an instance-field ???

    /* Can't instantiate. Just static methods. */ 
    private QueueUtil() { }

    /**
     * Used by RPC clients (e.g. {@link com.shopwiki.roger.rpc.RpcClient}).
     *
     * Same behavior as com.rabbitmq.client.impl.ChannelN.queueDeclare():
     * Non-durable,
     * exclusive,
     * auto-delete.
     */
    public static DeclareOk declareAnonymousQueue(Channel channel, Map<String,Object> args) throws IOException {
        final String queueName = "";
        return declareTempQueue(channel, queueName, args);
    }

    /**
     * Used by temporary (i.e. for testing) {@link com.shopwiki.roger.rpc.RpcServer}s.
     *
     * Non-durable,
     * exclusive,
     * auto-delete.
     */
    public static DeclareOk declareTempQueue(Channel channel, String queueName, Map<String,Object> args) throws IOException {
        final boolean durable = false;
        final boolean exclusive = queueName.equals("");
        final boolean autoDelete = true;
        return declareQueue(channel, queueName, durable, exclusive, autoDelete, args);
    }

    /**
     * Used by permanent (i.e. production) RpcServers.
     *
     * Durable,
     * non-exclusive,
     * non-auto-delete.
     */
    public static DeclareOk declareNamedQueue(Channel channel, String queueName, Map<String,Object> args) throws IOException {
        final boolean durable = true; // TODO: make configurable ???
        final boolean exclusive = false;
        final boolean autoDelete = false;
        return declareQueue(channel, queueName, durable, exclusive, autoDelete, args);
    }

    private static DeclareOk declareQueue(Channel channel, String queueName, boolean durable, boolean exclusive, boolean autoDelete, Map<String,Object> args) throws IOException {
        if (args == null) {
            args = Collections.emptyMap();
        }
        return channel.queueDeclare(queueName, durable, exclusive, autoDelete, args);
    }
}
