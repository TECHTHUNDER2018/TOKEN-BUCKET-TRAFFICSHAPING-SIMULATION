package com.trafficshaping.web.service;

import com.trafficshaping.network.client.BurstyClient;
import com.trafficshaping.network.client.RandomClient;
import com.trafficshaping.network.client.SteadyClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class ClientManagerService {

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final List<Future<?>> activeClients = new ArrayList<>();
    private final String serverHost;
    private final int serverPort;

    public ClientManagerService(@Value("${server.host:localhost}") String serverHost,
                                @Value("${tcp.port:9000}") int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    @PostConstruct
    public void init() {
        // Automatically start the classic 3 clients on boot
        spawnClients("steady", 1);
        spawnClients("bursty", 1);
        spawnClients("random", 1);
    }

    public synchronized int getActiveClientCount() {
        activeClients.removeIf(Future::isDone);
        return activeClients.size();
    }

    public synchronized void spawnClients(String type, int count) {
        for (int i = 0; i < count; i++) {
            Runnable clientRunnable = null;
            switch (type.toLowerCase()) {
                case "steady":
                    clientRunnable = new SteadyClient(serverHost, serverPort);
                    break;
                case "bursty":
                    clientRunnable = new BurstyClient(serverHost, serverPort);
                    break;
                case "random":
                    clientRunnable = new RandomClient(serverHost, serverPort);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown client type: " + type);
            }
            activeClients.add(executor.submit(clientRunnable));
        }
    }

    public synchronized void stopAll() {
        for (Future<?> f : activeClients) {
            f.cancel(true);
        }
        activeClients.clear();
    }

    @PreDestroy
    public void shutdown() {
        stopAll();
        executor.shutdownNow();
    }
}
