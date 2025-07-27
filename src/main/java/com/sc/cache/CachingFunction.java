package com.sc.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.concurrent.locks.ReentrantLock;


public class CachingFunction<K, V> implements Cache<K, V> {

    public static final String FUNCTION_NOT_NULL = "Function must not be null!";
    public static final String NOT_NULL_KEY = "Null Keys are not supported!";
    public static final String NOT_NULL_VALUE = "Function returned null for key: ";
    public static final String FUNCTION_COMPUTATION = "Function computation failed!";

    private final Map<K, Future<V>> cache;
    private final Function<K, V> function;

    private final Lock lock = new ReentrantLock();

    public CachingFunction(Function<K, V> function) {
        if (function == null){
            throw new NullPointerException(FUNCTION_NOT_NULL);
        }
        this.function = function;
        this.cache = new HashMap<>();
    }

    @Override
    public V get(K key) {
        if (key == null){
            throw new NullPointerException(NOT_NULL_KEY);
        }
        Future<V> future;
        boolean requiresComputation = false;
        lock.lock();
        try {
            future = cache.get(key);
            if (future == null) {
                future = new FutureTask<>(() -> function.apply(key));
                cache.put(key, future);
                requiresComputation = true;
            }
        } finally {
            lock.unlock();
        }
        if (requiresComputation) {
            ((FutureTask<V>) future).run();
        }
        try {
            V v = future.get();
            if (v == null) {
                removeFuture(key, future);
                throw new NullPointerException(NOT_NULL_VALUE + key);
            }
            return v;
        } catch (CancellationException | ExecutionException e) {
            // Remove the task if computation failed
            removeFuture(key, future);
            Throwable cause = e.getCause();
            throw new RuntimeException(FUNCTION_COMPUTATION, cause != null ? cause : e);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted!", e);
        }
    }
    private void removeFuture(K key, Future<V> future) {
        lock.lock();
        try {
            if (cache.get(key) == future) {
                cache.remove(key);
            }
        } finally {
            lock.unlock();
        }
    }
}

