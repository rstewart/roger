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
        ExampleEventHandler example = new ExampleEventHandler();
        example.manager.add(example.handler, ExampleEventHandler.ROUTE);
        example.manager.start();
        assertEvent(example);
        example.manager.stop();
    }

    @Test
    public void test2() throws Exception { // start then add handler
        ExampleEventHandler.declareExchange();
        ExampleEventHandler example = new ExampleEventHandler();
        example.manager.start();
        example.manager.add(example.handler, ExampleEventHandler.ROUTE);
        assertEvent(example);
        example.manager.stop();
    }

    private static void assertEvent(ExampleEventHandler example) throws IOException, InterruptedException {
        ExampleEventSender.sendEvent();
        Thread.sleep(1000);
        Assert.assertEquals("Hello Robert!", example.lastMessage);
    }
}
