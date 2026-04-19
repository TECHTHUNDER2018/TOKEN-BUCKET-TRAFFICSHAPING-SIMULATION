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

    private double fractionalLeak = 0;

    public void start() {
        // Run constantly over 100ms intervals (10.0x a second), dynamically adapting to refillRate
        scheduler.scheduleAtFixedRate(() -> {
            double toLeakDouble = config.getRefillRate() / 10.0;
            fractionalLeak += toLeakDouble;
            
            int toLeak = (int) fractionalLeak;
            fractionalLeak -= toLeak;
            
            for (int i = 0; i < toLeak; i++) {
                int current;
                do {
                    current = currentQueue.get();
                    if (current <= 0) break;
                } while (!currentQueue.compareAndSet(current, current - 1));
                
                if (current > 0) {
                    metrics.incrementLeakyPassed(); 
                } else {
                    break;
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
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
