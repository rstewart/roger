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

/**
 * A special Exception type a user may throw in their {@link RequestHandler}
 * if they wish for a {@link RequestConsumer} to nack a request.
 *
 * @author rstewart
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
