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
import java.util.concurrent.*;

/**
 * @owner rstewart
 */
public class PostProcessors {

    public static interface PostProcessor {
        public void process(
                String queueName,
                ResponseStatus responseStatus,
                Object request, Object response,
                Throwable thrown,
                long timeTaken
                ) throws Exception;
    }

    // TODO: Should there be only 1 executor ???
    private final Map<PostProcessor, ExecutorService> ppToExecutor = new LinkedHashMap<PostProcessor,ExecutorService>();

    public void add(PostProcessor pp) {
        ppToExecutor.put(pp, Executors.newFixedThreadPool(1));
    }

    public void process(
            final String queueName,
            final ResponseStatus responseStatus,
            final Object request,
            final Object response,
            final Throwable thrown,
            final long timeTaken
            ) {

        for (Map.Entry<PostProcessor, ExecutorService> entry : ppToExecutor.entrySet()) {
            final PostProcessor pp = entry.getKey();

            Callable<Void> task = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    pp.process(queueName, responseStatus, request, response, thrown, timeTaken);
                    return null;
                }
            };

            entry.getValue().submit(task);
        }
    }
}
