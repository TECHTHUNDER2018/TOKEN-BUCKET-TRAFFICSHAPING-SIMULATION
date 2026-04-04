package com.trafficshaping.core;

import java.util.concurrent.atomic.AtomicLong;

public class MetricsBucket {
    private final AtomicLong passedPackets = new AtomicLong(0);
    private final AtomicLong droppedPackets = new AtomicLong(0);
    private final AtomicLong leakyPassed = new AtomicLong(0);
    private final AtomicLong leakyDropped = new AtomicLong(0);

    public void incrementPassed() { passedPackets.incrementAndGet(); }
    public void incrementDropped() { droppedPackets.incrementAndGet(); }
    public void incrementLeakyPassed() { leakyPassed.incrementAndGet(); }
    public void incrementLeakyDropped() { leakyDropped.incrementAndGet(); }

    public long getPassed() { return passedPackets.get(); }
    public long getDropped() { return droppedPackets.get(); }
    public long getLeakyPassed() { return leakyPassed.get(); }
    public long getLeakyDropped() { return leakyDropped.get(); }
    
    public void reset() {
        passedPackets.set(0);
        droppedPackets.set(0);
        leakyPassed.set(0);
        leakyDropped.set(0);
    }
}
