package com.trafficshaping.web.controller;

import com.trafficshaping.web.service.ClientManagerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientManagerService clientManager;

    public ClientController(ClientManagerService clientManager) {
        this.clientManager = clientManager;
    }

    @GetMapping("/count")
    public int getActiveCount() {
        return clientManager.getActiveClientCount();
    }

    @PostMapping("/spawn/{type}")
    public void spawnClients(@PathVariable("type") String type, @RequestParam(name = "count", defaultValue = "1") int count) {
        clientManager.spawnClients(type, count);
    }

    @PostMapping("/stop")
    public void stopAll() {
        clientManager.stopAll();
    }
}
