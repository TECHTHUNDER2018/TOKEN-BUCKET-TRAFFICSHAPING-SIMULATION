package com.trafficshaping.core;

import java.util.concurrent.atomic.AtomicInteger;

public class EngineConfig {
    // Capacity of the bucket
    private final AtomicInteger bucketCapacity = new AtomicInteger(100);
    
    // Tokens generated per second
    private final AtomicInteger refillRate = new AtomicInteger(50);

    public int getBucketCapacity() { return bucketCapacity.get(); }
    public void setBucketCapacity(int capacity) { this.bucketCapacity.set(capacity); }

    public int getRefillRate() { return refillRate.get(); }
    public void setRefillRate(int rate) { this.refillRate.set(rate); }
}
