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

import java.util.*;

import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.shopwiki.roger.*;

/**
 * @owner rstewart
 */
@JsonPropertyOrder(value={"exceptionName", "exceptionMsg", "exceptionStackTrace", "cause"})
public class ExceptionResponse extends AbstractMessage {

    private final String exceptionName;
    private final String exceptionMsg;
    private final List<String> stackTrace;
    private final ExceptionResponse cause;

    public ExceptionResponse(Throwable e) {
        exceptionName = e.getClass().getName();
        exceptionMsg = e.getMessage();

        stackTrace = new ArrayList<String>();
        for (StackTraceElement ste : e.getStackTrace()) {
            stackTrace.add(ste.toString());
        }

        if (MessagingUtil.DEBUG) {
            e.printStackTrace();
        }

        if (e.getCause() != null) {
            cause = new ExceptionResponse(e.getCause());
        } else {
            cause = null;
        }
    }

    public String getExceptionName() {
        return exceptionName;
    }

    public String getExceptionMsg() {
        return exceptionMsg;
    }

    public List<String> getStackTrace() {
        return Collections.unmodifiableList(stackTrace);
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public ExceptionResponse getCause() {
        return cause;
    }
}
