package com.sc.cache;

public interface Cache <K,V>{
    V get(K key);
}
