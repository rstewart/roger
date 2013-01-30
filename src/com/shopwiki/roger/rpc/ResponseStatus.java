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
 * Response codes used by {@link RequestConsumer}.
 *
 * <TABLE>
 * <TR><TD>OK</TD><TD>The request was handled normally.</TD></TR>
 * <TR><TD>MALFORMED_REQUEST</TD><TD>An Exception was thrown while attempting deserialize the request from JSON into a Java Object.</TD></TR>
 * <TR><TD>INVALID_REQUEST</TD><TD>The {@link RequestHandler} threw an {@link IllegalArgumentException}.</TD></TR>
 * <TR><TD>HANDLER_ERROR</TD><TD>The {@link RequestHandler} threw an {@link Exception}.</TD></TR>
 * <TR><TD>NACK</TD><TD>Never returned, only for logging.  Indicates the request was nacked.</TD></TR>
 * <TR><TD>NUCLEAR</TD><TD>Never returned, only for logging.  Indicates the {@link RequestConsumer} encountered an unexpected error.</TD></TR>
 * </TABLE>
 *
 * @author rstewart
 */
public enum ResponseStatus {
    OK,
    MALFORMED_REQUEST,
    INVALID_REQUEST,
    HANDLER_ERROR,
    NACK,
    NUCLEAR;
}
