package com.shopwiki.roger.event;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.shopwiki.roger.example.ExampleEventHandler;
import com.shopwiki.roger.example.ExampleEventSender;

public class EventTest {

    @Test
    public void test1() throws Exception { // add handler then start
        ExampleEventHandler.declareExchange();
        ExampleEventHandler.manager.add(ExampleEventHandler.handler, ExampleEventHandler.ROUTE);
        ExampleEventHandler.manager.start();
        assertEvent();
        ExampleEventHandler.manager.stop();
    }

    @Test
    public void test2() throws Exception { // start then add handler
        ExampleEventHandler.declareExchange();
        ExampleEventHandler.manager.start();
        ExampleEventHandler.manager.add(ExampleEventHandler.handler, ExampleEventHandler.ROUTE);
        assertEvent();
        ExampleEventHandler.manager.stop();
    }

    private static void assertEvent() throws IOException {
        ExampleEventSender.sendEvent();
        Assert.assertEquals("Hello Robert!", ExampleEventHandler.lastMessage);        
    }
}
