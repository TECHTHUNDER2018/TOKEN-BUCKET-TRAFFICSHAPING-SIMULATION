package com.trafficshaping.web.config;

import com.trafficshaping.core.EngineConfig;
import com.trafficshaping.core.LeakyBucket;
import com.trafficshaping.core.MetricsBucket;
import com.trafficshaping.core.TokenBucket;
import com.trafficshaping.network.server.TcpServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class AppConfig {

    @Bean
    public EngineConfig engineConfig() {
        return new EngineConfig();
    }

    @Bean
    public MetricsBucket metricsBucket() {
        return new MetricsBucket();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public TokenBucket tokenBucket(EngineConfig engineConfig, MetricsBucket metricsBucket) {
        return new TokenBucket(engineConfig, metricsBucket);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public LeakyBucket leakyBucket(EngineConfig engineConfig, MetricsBucket metricsBucket) {
        return new LeakyBucket(engineConfig, metricsBucket);
    }

    @Bean(destroyMethod = "stop")
    public TcpServer tcpServer(TokenBucket tokenBucket, LeakyBucket leakyBucket, @org.springframework.beans.factory.annotation.Value("${tcp.port:9000}") int tcpPort) {
        TcpServer server = new TcpServer(tcpPort, tokenBucket, leakyBucket);
        new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return server;
    }
}
