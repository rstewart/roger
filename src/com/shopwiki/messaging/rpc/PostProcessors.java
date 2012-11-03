package com.shopwiki.messaging.rpc;

import java.util.*;
import java.util.concurrent.*;

/**
 * @owner rstewart
 */
public class PostProcessors {

    public static interface PostProcessor {
        public void process(String queueName, ResponseStatus responseStatus, Object request, Object response, Throwable thrown) throws Exception;
    }

    private final Map<PostProcessor, ExecutorService> ppToExecutor = new LinkedHashMap<PostProcessor,ExecutorService>();

    public void add(PostProcessor pp) {
        ppToExecutor.put(pp, Executors.newFixedThreadPool(1));
    }

    public void process(final String queueName, final ResponseStatus responseStatus, final Object request, final Object response, final Throwable thrown) {
        for (Map.Entry<PostProcessor, ExecutorService> entry : ppToExecutor.entrySet()) {
            final PostProcessor pp = entry.getKey();

            Callable<Void> task = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    pp.process(queueName, responseStatus, request, response, thrown);
                    return null;
                }
            };

            entry.getValue().submit(task);
        }
    }
}
