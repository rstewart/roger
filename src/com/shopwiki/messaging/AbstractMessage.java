package com.shopwiki.messaging;

import org.codehaus.jackson.annotate.*;

/**
 * @owner rstewart
 */
public abstract class AbstractMessage {

    @Override
    public String toString() {
        return MessagingUtil.prettyPrintMessage(this);
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static abstract class NonStrictJsonMessage extends AbstractMessage { }
}
