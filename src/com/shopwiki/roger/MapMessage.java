package com.shopwiki.roger;

import java.util.LinkedHashMap;

import org.codehaus.jackson.type.TypeReference;


/**
 * Mainly for debugging.
 * Better to make your own class extending BasicJsonMessage.
 *
 * @owner rstewart
 */
public class MapMessage extends LinkedHashMap<String,Object> {

    private static final long serialVersionUID = 2223888156798485002L;

    public static final TypeReference<MapMessage> TYPE_REF = new TypeReference<MapMessage>() { };

    @Override
    public String toString() {
        return MessagingUtil.prettyPrintMessage(this);
    }
}
