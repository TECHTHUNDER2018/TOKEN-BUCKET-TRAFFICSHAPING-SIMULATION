package com.trafficshaping.web.service;

import com.trafficshaping.core.MetricsBucket;
import com.trafficshaping.core.TokenBucket;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class TelemetryPublisher {

    private final TelemetryHandler telemetryHandler;
    private final TokenBucket tokenBucket;
    private final MetricsBucket metricsBucket;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private long lastPassed = 0;
    private long lastDropped = 0;
    private long lastLeakyPassed = 0;
    private long lastLeakyDropped = 0;

    public TelemetryPublisher(TelemetryHandler telemetryHandler, TokenBucket tokenBucket, MetricsBucket metricsBucket) {
        this.telemetryHandler = telemetryHandler;
        this.tokenBucket = tokenBucket;
        this.metricsBucket = metricsBucket;
    }

    @Scheduled(fixedRate = 500)
    @SuppressWarnings("null")
    public void publishMetrics() {
        String timestamp = dateFormat.format(new Date());
        long currentPassed = metricsBucket.getPassed();
        long currentDropped = metricsBucket.getDropped();
        long currentLeakyPassed = metricsBucket.getLeakyPassed();
        long currentLeakyDropped = metricsBucket.getLeakyDropped();
        
        long deltaPassed = currentPassed - lastPassed;
        long deltaDropped = currentDropped - lastDropped;
        long deltaLeakyPassed = currentLeakyPassed - lastLeakyPassed;
        long deltaLeakyDropped = currentLeakyDropped - lastLeakyDropped;
        
        lastPassed = currentPassed;
        lastDropped = currentDropped;
        lastLeakyPassed = currentLeakyPassed;
        lastLeakyDropped = currentLeakyDropped;
        
        int bucketLevel = tokenBucket.getCurrentTokens();

        String json = String.format("{\"timestamp\": \"%s\", \"passed\": %d, \"dropped\": %d, \"leakyPassed\": %d, \"leakyDropped\": %d, \"bucketLevel\": %d}", 
                                    timestamp, deltaPassed, deltaDropped, deltaLeakyPassed, deltaLeakyDropped, bucketLevel);
        
        telemetryHandler.broadcast(json);
    }
}
