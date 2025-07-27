package com.sc.cache;

import com.sc.dlengine.DeadlineScheduler;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DeadlineSchedulerTest {

    @Test
    public void testScheduleAndPoll() {
        DeadlineScheduler engine = new DeadlineScheduler();
        long now = System.currentTimeMillis();

        long id1 = engine.schedule(now + 100);
        long id2 = engine.schedule(now + 200);

        assertEquals(2, engine.size());

        List<Long> expired = new ArrayList<>();
        int fired = engine.poll(now + 150, expired::add, 10);

        assertEquals(1, fired);
        assertTrue(expired.contains(id1));
        assertFalse(expired.contains(id2));
        assertEquals(1, engine.size());
    }

    @Test
    public void testCancel() {
        DeadlineScheduler engine = new DeadlineScheduler();
        long now = System.currentTimeMillis();
        long id = engine.schedule(now + 500);

        assertTrue(engine.cancel(id));
        assertEquals(0, engine.size());

        List<Long> expired = new ArrayList<>();
        int fired = engine.poll(now + 1000, expired::add, 10);
        assertEquals(0, fired);
    }

    @Test
    public void testPollRespectsMaxPoll() {
        DeadlineScheduler engine = new DeadlineScheduler();
        long now = System.currentTimeMillis();

        long id1 = engine.schedule(now + 50);
        long id2 = engine.schedule(now + 60);
        long id3 = engine.schedule(now + 70);

        List<Long> expired = new ArrayList<>();
        int fired = engine.poll(now + 100, expired::add, 2);

        assertEquals(2, fired);
        assertEquals(1, engine.size());

        List<Long> expiredLater = new ArrayList<>();
        int firedLater = engine.poll(now + 100, expiredLater::add, 2);
        assertEquals(1, firedLater);
    }

    @Test
    public void testNoDeadlinesFiredBeforeTime() {
        DeadlineScheduler engine = new DeadlineScheduler();
        long now = System.currentTimeMillis();
        engine.schedule(now + 500);

        List<Long> expired = new ArrayList<>();
        int fired = engine.poll(now + 100, expired::add, 10);
        assertEquals(0, fired);
        assertEquals(1, engine.size());
    }
}
