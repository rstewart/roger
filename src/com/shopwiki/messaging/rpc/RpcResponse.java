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
