package com.shopwiki.messaging.rpc;

import java.util.*;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.shopwiki.messaging.*;

/**
 * Only used by the RpcClient
 *
 * @owner rstewart
 */
public class RpcResponse {

    private BasicProperties props;
    private MapMessage body; // Make generic ???
    private ResponseStatus status;

    public RpcResponse(BasicProperties props, MapMessage body) {
        this.props = props;
        this.body = body;

        Object statusObj = getHeaders().get("status");
        String statusStr = String.valueOf(statusObj);
        status = ResponseStatus.valueOf(statusStr);
    }

    public Map<String, Object> getHeaders() {
        Map<String,Object> headers = props.getHeaders();
        if (headers == null) {
            return Collections.emptyMap();
        }
        return headers;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public MapMessage getBody() {
        return body;
    }
}
