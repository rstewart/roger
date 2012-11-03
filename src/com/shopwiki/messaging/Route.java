package com.shopwiki.messaging;

/**
 * @owner rstewart
 */
public class Route {

    public final String exchange;
    public final String key;

    public Route(String exchange, String key) {
        this.exchange = exchange;
        this.key = key;
    }
}
