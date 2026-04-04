package com.trafficshaping.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LeakyBucket {
    private final AtomicInteger currentQueue;
    private final EngineConfig config;
    private final MetricsBucket metrics;
    private final ScheduledExecutorService scheduler;

    public LeakyBucket(EngineConfig config, MetricsBucket metrics) {
        this.config = config;
        this.metrics = metrics;
        this.currentQueue = new AtomicInteger(0);
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        // Run frequently to check if we need to leak based on rate
        scheduler.scheduleAtFixedRate(() -> {
            int current;
            do {
                current = currentQueue.get();
                if (current <= 0) break;
            } while (!currentQueue.compareAndSet(current, current - 1));
            
            if (current > 0) {
                metrics.incrementLeakyPassed(); 
            }
        }, 0, Math.max(10, 1000 / Math.max(1, config.getRefillRate())), TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    public boolean processPacket(Packet packet) {
        while (true) {
            int current = currentQueue.get();
            if (current < config.getBucketCapacity()) {
                if (currentQueue.compareAndSet(current, current + 1)) {
                    return true;
                }
            } else {
                metrics.incrementLeakyDropped();
                return false; 
            }
        }
    }

    public int getCurrentQueue() {
        return currentQueue.get();
    }
}
