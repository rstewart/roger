package com.shopwiki.messaging.rpc;

/**
 * @owner rstewart
 */
public enum ResponseStatus {
    OK,
    MALFORMED_REQUEST,
    INVALID_REQUEST,
    HANDLER_ERROR,
    // never returned, only for logging
    NACK,
    NUCLEAR,
    ;
}
