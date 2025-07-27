package com.sc.cache;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class CachingFunctionTest {
   @Test
   public void testCacheReturnsSameValueForSameKey() {
        Cache<String, String> cache = new CachingFunction<>(key -> "Processed-" + key);

        String val1 = cache.get("A");
        String val2 = cache.get("A");

        assertEquals("Processed-A", val1);
        assertSame(val1, val2);
    }

    @Test
    public void testFunctionOnlyCalledOncePerKey() throws InterruptedException {
        AtomicInteger callCount = new AtomicInteger();
        Cache<String, String> cache = new CachingFunction<>(key -> {
            callCount.incrementAndGet();
            return "value-" + key;
        });
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                cache.get("X");
                latch.countDown();
            });
        }
        latch.await();
        executor.shutdownNow();
        assertEquals("Function only called once per key", 1, callCount.get());
    }

    @Test
    public void testNullKeyThrowsException() {
        Cache<String, String> cache = new CachingFunction<>(key -> "randomKey");
        assertThrows(NullPointerException.class, () -> cache.get(null));
    }

    @Test
    public void testNullValueThrowsException() {
        Cache<String, String> cache = new CachingFunction<>(key -> {
            throw new NullPointerException("Null value");
        });
        RuntimeException ex = assertThrows(RuntimeException.class, () -> cache.get("fail"));
        assertTrue(ex.getCause() instanceof NullPointerException);
    }

    @Test
    public void testFunctionThrowsWrappedAsRuntimeException() {
        Cache<String, String> cache = new CachingFunction<>(key -> {
            throw new IllegalStateException("Boom");
        });

        RuntimeException ex = assertThrows(RuntimeException.class, () -> cache.get("fail"));
        assertTrue(ex.getCause() instanceof IllegalStateException);
    }
}
