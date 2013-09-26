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

import java.util.*;

import com.google.common.collect.Maps;
import com.rabbitmq.client.AMQP.BasicProperties;

/**
 * Used by {@link RpcClient} to conveniently bundle the headers & body of an RPC response.
 *
 * @author rstewart
 */
public class RpcResponse<T> {

    private final BasicProperties props;
    private final T body;
    private final ResponseStatus status;

    public RpcResponse(BasicProperties props, T body) {
        this.props = props;
        this.body = body;
        this.status = getStatus(props);
    }

    public static Map<String, String> getHeaders(BasicProperties props) {
        Map<String,Object> headers = props.getHeaders();
        if (headers == null) {
            return Collections.emptyMap();
        }

        Map<String, String> map = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            map.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return map;
    }

    public static ResponseStatus getStatus(BasicProperties props) {
        String statusStr = getHeaders(props).get("status");
        return ResponseStatus.valueOf(statusStr);
    }

    public Map<String, String> getHeaders() {
        return getHeaders(props);
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public T getBody() {
        return body;
    }
}
