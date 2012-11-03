package com.shopwiki.messaging.rpc;

/**
 * @owner rstewart
 */
public class NackException extends Exception {

    private static final long serialVersionUID = 3609423674773742315L;

    public NackException(String message) {
        super(message);
    }

    public NackException(String message, Throwable cause) {
        super(message, cause);
    }
}
