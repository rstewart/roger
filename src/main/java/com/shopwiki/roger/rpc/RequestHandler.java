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

import org.codehaus.jackson.type.TypeReference;

/**
 * This interface is left for the user to implement.
 * Instances of this are generally passed into a {@link RpcServer.WorkerFactory}
 * and used to create {@link RpcWorker}s.
 *
 * @param <I> request type (I for input).
 * @param <O> response type (O for output).
 *
 * @author rstewart
 */
public interface RequestHandler<I,O> {

    // TODO: WHY doesn't this work ???
    //private final TypeReference<T> typeRef = new TypeReference<T>() { };
    // ...oh Well, make them implement one more method.
    /**
     * @return A TypeReference used by Jackson to know what class to instantiate when deserializing requests.
     */
    TypeReference<I> getRequestType();

    /**
     * @param request Deserialized from JSON.
     * @return response Serialized to JSON.
     * @throws Exception Some types of Exceptions produce a special {@link ResponseStatus}.
     * {@link IllegalArgumentException} -> INVALID_REQUEST
     * {@link NackException} -> NACK
     */
    O handleRequest(I request) throws Exception;
}
