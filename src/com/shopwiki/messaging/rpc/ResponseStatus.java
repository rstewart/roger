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
