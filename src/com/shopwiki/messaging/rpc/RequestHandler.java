package com.shopwiki.messaging.rpc;

import org.codehaus.jackson.type.TypeReference;

/**
 * @owner rstewart
 */
public interface RequestHandler<I,O> {

    // TODO: WHY doesn't this work ???
    //private final TypeReference<T> typeRef = new TypeReference<T>() { };
    // ...oh Well, make them implement one more method.
    public TypeReference<I> getRequestType();

    public O handleRequest(I request) throws Exception;
}
