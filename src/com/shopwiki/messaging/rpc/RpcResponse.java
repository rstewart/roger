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

package com.shopwiki.messaging.rpc;

import java.util.*;

import com.google.common.collect.Maps;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.shopwiki.messaging.*;

/**
 * Only used by the RpcClient
 *
 * @owner rstewart
 */
public class RpcResponse {

    private final BasicProperties props;
    private final MapMessage body; // Make generic ???
    private final ResponseStatus status;

    public RpcResponse(BasicProperties props, MapMessage body) {
        this.props = props;
        this.body = body;

        String statusStr = getHeaders().get("status");
        status = ResponseStatus.valueOf(statusStr);
    }

    public Map<String, String> getHeaders() {
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

    public ResponseStatus getStatus() {
        return status;
    }

    public MapMessage getBody() {
        return body;
    }
}
