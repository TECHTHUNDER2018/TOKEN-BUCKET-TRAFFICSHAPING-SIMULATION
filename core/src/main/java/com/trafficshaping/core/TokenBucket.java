package com.trafficshaping.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TokenBucket {
    private final AtomicInteger currentTokens;
    private final EngineConfig config;
    private final MetricsBucket metrics;
    private final ScheduledExecutorService scheduler;

    public TokenBucket(EngineConfig config, MetricsBucket metrics) {
        this.config = config;
        this.metrics = metrics;
        this.currentTokens = new AtomicInteger(config.getBucketCapacity());
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        // Refill loop: add refillRate tokens every 1 second, but done continuously over 100ms intervals (10x a second)
        scheduler.scheduleAtFixedRate(() -> {
            int toAdd = Math.max(1, config.getRefillRate() / 10);
            int capacity = config.getBucketCapacity();
            
            int current, next;
            do {
                current = currentTokens.get();
                next = Math.min(capacity, current + toAdd);
            } while (current != next && !currentTokens.compareAndSet(current, next));
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    public boolean processPacket(Packet packet) {
        int requiredTokens = 1; // 1 token per packet
        
        while (true) {
            int current = currentTokens.get();
            if (current >= requiredTokens) {
                if (currentTokens.compareAndSet(current, current - requiredTokens)) {
                    metrics.incrementPassed();
                    return true;
                }
            } else {
                metrics.incrementDropped();
                return false;
            }
        }
    }
    
    public int getCurrentTokens() {
        return currentTokens.get();
    }
    
    public MetricsBucket getMetrics() {
        return metrics;
    }
    
    public EngineConfig getConfig() {
        return config;
    }
}
