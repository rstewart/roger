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

package com.shopwiki.roger.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copied from shopwiki repo for use in {@link com.shopwiki.roger.RabbitConnector}.
 *
 * @author jdickinson
 */
public final class DaemonThreadFactory {

    /* Can't instantiate. Just static methods. */
    private DaemonThreadFactory() { }

    private static final ThreadFactory INSTANCE = new ThreadFactory() {
        @Override
		public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    };

    public static ThreadFactory getInstance() {
        return INSTANCE;
    }

    public static ThreadFactory getInstance(final String name, final boolean appendCount) {
        ThreadFactory foo = new ThreadFactory() {
            private AtomicInteger ai = new AtomicInteger(1);
            @Override
			public Thread newThread(Runnable r) {
                Thread t;
                if (appendCount) {
                    t = new Thread(r, name + "-" + ai.getAndIncrement());
                } else {
                    t = new Thread(r, name);
                }
                t.setDaemon(true);
                return t;
            }
        };
        return foo;
    }
}
