package com.sc.dlengine;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class DeadlineSchedulerBenchmark {

    private DeadlineEngine engine;
    private long[] scheduledIds;
    private int index;

    @Setup(Level.Iteration)
    public void setup() {
        engine = new DeadlineScheduler();
        scheduledIds = new long[1000];
        long base = System.currentTimeMillis();

        for (int i = 0; i < scheduledIds.length; i++) {
            scheduledIds[i] = engine.schedule(base + ThreadLocalRandom.current().nextInt(1000));
        }

        index = 0;
    }

    @Benchmark
    public long benchmarkSchedule() {
        return engine.schedule(System.currentTimeMillis() + 500);
    }

    @Benchmark
    public boolean benchmarkCancel() {
        index = (index + 1) % scheduledIds.length;
        return engine.cancel(scheduledIds[index]);
    }

    @Benchmark
    public int benchmarkPoll() {
        long now = System.currentTimeMillis() + 1000;
        return engine.poll(now, id -> {
            // No-op handler
        }, 10);
    }

    @Benchmark
    public int benchmarkSize() {
        return engine.size();
    }
}

