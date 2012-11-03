package com.shopwiki.messaging.rpc;

import java.util.*;

import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.shopwiki.messaging.*;

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
