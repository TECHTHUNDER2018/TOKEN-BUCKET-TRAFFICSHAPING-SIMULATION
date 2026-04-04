package com.trafficshaping.core;

public class Packet {
    private final String id;
    private final int size;
    private final long timestamp;

    public Packet(String id, int size) {
        this.id = id;
        this.size = size;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public int getSize() { return size; }
    public long getTimestamp() { return timestamp; }
}
