package com.ramika.habit.service;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Runs a task once at the next local midnight, then every 24h. */
public final class MidnightScheduler {
    private MidnightScheduler() {}

    private static ScheduledExecutorService exec;

    public static synchronized void start(Runnable task) {
        stop(); // idempotent
        exec = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "midnight-scheduler");
            t.setDaemon(true);
            return t;
        });
        long initialDelayMs = millisUntilNextMidnight();
        long periodMs = Duration.ofDays(1).toMillis();
        exec.scheduleAtFixedRate(task, initialDelayMs, periodMs, TimeUnit.MILLISECONDS);
    }

    public static synchronized void stop() {
        if (exec != null) {
            exec.shutdownNow();
            exec = null;
        }
    }

    private static long millisUntilNextMidnight() {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone);
        return Duration.between(now, nextMidnight).toMillis();
    }
}
