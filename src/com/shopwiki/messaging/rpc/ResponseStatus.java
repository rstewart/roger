package com.shopwiki.messaging.rpc;

/**
 * @owner rstewart
 */
public enum ResponseStatus {
    OK,
    MALFORMED_REQUEST, // An Exception was thrown while attempting deserialize the request from JSON into a Java Object
    INVALID_REQUEST, // The RequestHandler threw an IllegalArgumentException
    HANDLER_ERROR, // The RequestHandler threw an Exception

    // never returned, only for logging
    NACK,
    NUCLEAR,
    ;
}
