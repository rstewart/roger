package com.shopwiki.messaging.rpc;

import java.util.*;

import com.rabbitmq.client.Connection;

/**
 * @owner rstewart
 */
public class RpcWorkers implements Iterable<RpcWorker> {

    private final Connection conn;
    private final List<RpcWorker> list = new ArrayList<RpcWorker>();

    public RpcWorkers(Connection conn) {
        this.conn = conn;
    }

    public Connection getConnection() {
        return conn;
    }

    public void add(RpcWorker worker) {
        list.add(worker);
    }

    @Override
    public Iterator<RpcWorker> iterator() {
        return list.iterator();
    }
}
