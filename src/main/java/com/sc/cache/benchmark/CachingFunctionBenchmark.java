package com.sc.cache.benchmark;

import com.sc.cache.CachingFunction;
import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class CachingFunctionBenchmark {

    private CachingFunction<Integer, String> cachingFunction;
    private Random random;

    @Param({"100", "1000", "10000"})
    private int keyRange;

    @Setup(Level.Iteration)
    public void setup() {
        Function<Integer, String> expensiveFunction = key -> {
            // Simulating expensive computation
            try {
                Thread.sleep(1); // simulate 1ms delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Value" + key;
        };
        cachingFunction = new CachingFunction<>(expensiveFunction);
        random = new Random(123);
    }

    @Benchmark
    public String testRepeatedSameKey() {
        return cachingFunction.get(42);
    }

    @Benchmark
    public String testRandomKeys() {
        return cachingFunction.get(random.nextInt(keyRange));
    }

    @Benchmark
    public String testHalfMissHalfHit() {
        int key = random.nextBoolean() ? 42 : random.nextInt(keyRange);
        return cachingFunction.get(key);
    }
}

