package com.sc.dlengine;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class DeadlineScheduler implements DeadlineEngine{

    private final PriorityQueue<Deadline> queue = new PriorityQueue<>();
    private final Map<Long, Deadline> idToDeadline = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public synchronized long schedule(long deadlineMs) {
        long id = idGenerator.getAndIncrement();
        Deadline deadline = new Deadline(id, deadlineMs);
        queue.offer(deadline);
        idToDeadline.put(id, deadline);
        return id;
    }

    @Override
    public synchronized boolean cancel(long requestId) {
        Deadline deadline = idToDeadline.remove(requestId);
        if (deadline != null) {
            return queue.remove(deadline);
        }
        return false;
    }

    @Override
    public synchronized int poll(long nowMs, Consumer<Long> handler, int maxPoll) {
        int triggered = 0;
        while (triggered < maxPoll && !queue.isEmpty()) {
            Deadline next = queue.peek();
            if (next.deadlineMs > nowMs) {
                break;
            }
            queue.poll();  // remove from queue
            idToDeadline.remove(next.id); // remove from map
            handler.accept(next.id); // fire callback
            triggered++;
        }
        return triggered;
    }

    @Override
    public synchronized int size() {
        return idToDeadline.size();
    }
    private static class Deadline implements Comparable<Deadline> {
        final long id;
        final long deadlineMs;

        Deadline(long id, long deadlineMs) {
            this.id = id;
            this.deadlineMs = deadlineMs;
        }

        @Override
        public int compareTo(Deadline other) {
            return Long.compare(this.deadlineMs, other.deadlineMs);
        }
    }
}
