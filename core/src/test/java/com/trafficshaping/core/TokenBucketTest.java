package com.trafficshaping.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TokenBucketTest {

    private TokenBucket tokenBucket;
    private EngineConfig config;
    private MetricsBucket metrics;

    @BeforeEach
    void setUp() {
        config = new EngineConfig();
        config.setBucketCapacity(10);
        config.setRefillRate(20);
        metrics = new MetricsBucket();
        tokenBucket = new TokenBucket(config, metrics);
    }

    @AfterEach
    void tearDown() {
        tokenBucket.stop();
    }

    @Test
    void testInitialCapacity() {
        assertEquals(10, tokenBucket.getCurrentTokens());
    }

    @Test
    void testProcessPacket_Success() {
        Packet packet = new Packet("p1", 100);
        assertTrue(tokenBucket.processPacket(packet));
        assertEquals(9, tokenBucket.getCurrentTokens());
        assertEquals(1, metrics.getPassed());
        assertEquals(0, metrics.getDropped());
    }

    @Test
    void testProcessPacket_Drop() {
        // Drain the bucket
        for (int i = 0; i < 10; i++) {
            assertTrue(tokenBucket.processPacket(new Packet("p" + i, 100)));
        }
        
        assertEquals(0, tokenBucket.getCurrentTokens());
        
        // Next packet should be dropped
        assertFalse(tokenBucket.processPacket(new Packet("drop", 100)));
        assertEquals(1, metrics.getDropped());
    }

    @Test
    void testRefill() throws InterruptedException {
        tokenBucket.start();
        
        // Drain
        for (int i = 0; i < 10; i++) {
            tokenBucket.processPacket(new Packet("p" + i, 100));
        }
        assertEquals(0, tokenBucket.getCurrentTokens());
        
        // Wait for refill (refill rate is 20/s => 2 tokens every 100ms)
        Thread.sleep(150);
        
        assertTrue(tokenBucket.getCurrentTokens() > 0);
        assertTrue(tokenBucket.getCurrentTokens() <= 10);
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        int threads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    tokenBucket.processPacket(new Packet(Thread.currentThread().getName(), 100));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        // Start all threads simultaneously
        latch.countDown();
        done.await(5, TimeUnit.SECONDS);

        // Bucket started with 10 tokens. 20 packets requested concurrently.
        // We should have exactly 10 passed and 10 dropped.
        assertEquals(10, metrics.getPassed(), "Passed packets must be exactly 10");
        assertEquals(10, metrics.getDropped(), "Dropped packets must be exactly 10");
        assertEquals(0, tokenBucket.getCurrentTokens(), "Bucket must be empty");
        
        executor.shutdown();
    }
}
