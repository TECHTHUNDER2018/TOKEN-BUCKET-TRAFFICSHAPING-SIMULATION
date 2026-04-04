package com.trafficshaping.web.controller;

import com.trafficshaping.core.EngineConfig;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final EngineConfig engineConfig;

    public ConfigController(EngineConfig engineConfig) {
        this.engineConfig = engineConfig;
    }

    @GetMapping
    public ConfigResponse getConfig() {
        return new ConfigResponse(engineConfig.getBucketCapacity(), engineConfig.getRefillRate());
    }

    @PostMapping
    public void updateConfig(@RequestBody ConfigRequest request) {
        engineConfig.setBucketCapacity(request.capacity());
        engineConfig.setRefillRate(request.refillRate());
    }

    public record ConfigResponse(int capacity, int refillRate) {}
    public record ConfigRequest(int capacity, int refillRate) {}
}
