package com.shopwiki.roger.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copied from shopwiki
 *
 * @owner jdickinson
 */
public final class DaemonThreadFactory {

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
