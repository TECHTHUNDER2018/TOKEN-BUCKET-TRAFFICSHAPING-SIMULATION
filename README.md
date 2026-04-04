# Token Bucket Traffic Shaping Simulator

A real-time, highly concurrent simulation of the Token Bucket (and Leaky Bucket) traffic shaping algorithms built with a true Full Stack Network architecture.

## Overview
This project visualizes algorithmic traffic shaping using:
1. **Core Java Algorithm**: Strict, decoupled token bucket and leaky bucket implementations using `AtomicInteger` and `ScheduledExecutorService` for thread-safe concurrency.
2. **Network Layer**: A Java `ServerSocket` TCP wrapper alongside three traffic-generating clients (Steady, Bursty, Random).
3. **Web & Analytics Layer**: Spring Boot serving a static web dashboard and a WebSocket endpoint broadcasting polling metrics. Vanilla JS + Chart.js powers the dashboard.

## Requirements
- Java 17+
- Maven
- (Optional) Docker & docker-compose

## How to Run Locally (Manual)

### 1. Build the Project
```bash
mvn clean install
```

### 2. Start the Server (Spring Boot + TCP Server)
```bash
java -jar web/target/web-1.0-SNAPSHOT.jar
```
The server will start on port 8080 (Web/WebSocket) and 9000 (TCP Sockets).
Open `http://localhost:8080/index.html` in your browser.

### 3. Run Traffic Generators
Open new terminal windows and run any of the client tests:
```bash
# Wait to run these until you have the dashboard open!
mvn exec:java -pl network -Dexec.mainClass="com.trafficshaping.network.client.SteadyClient"
mvn exec:java -pl network -Dexec.mainClass="com.trafficshaping.network.client.BurstyClient"
mvn exec:java -pl network -Dexec.mainClass="com.trafficshaping.network.client.RandomClient"
```

## How to Run (Docker Compose)
To run the full simulation in isolated containers:
```bash
docker-compose up --build
```
This spins up the Server and Web Dashboard, plus one instance of each client (Steady, Bursty, Random) throwing simultaneous traffic at the server.

## Features
- **Concurrent Engine**: Lock-free atomic variables and threads ensure thread safety.
- **WebSocket Telemetry**: Receives metrics polling payload from the core engine at 2Hz.
- **"Hot" Configuration**: Adjust the web UI sliders to instantly update the running Java algorithm via REST POST endpoint.
- **Showdown (Token vs Leaky)**: Plots the difference between a Token Bucket's handling of bursty traffic versus a Leaky Bucket's strict rate-limiting.
