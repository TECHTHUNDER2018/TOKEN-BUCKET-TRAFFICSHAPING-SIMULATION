#!/bin/bash
echo "Starting Token Bucket Traffic Shaping Simulator Super-Container..."

# Start the Spring Boot Web Server in the background
echo "Booting Web Dashboard and TCP Socket Engine..."
java -jar app.jar &

# Wait for Spring Boot to fully initialize its ServerSocket context
echo "Waiting 10 seconds for TCP Server bounds..."
sleep 10

echo "Routing Internal Traffic Generators to localhost:9000..."

# Explicitly set the host and port environment variables to hit the local container address
export SERVER_HOST=localhost
export SERVER_PORT=9000

# Start clients in the background
java -cp "/app/core-classes:/app/network-classes" com.trafficshaping.network.client.SteadyClient &
java -cp "/app/core-classes:/app/network-classes" com.trafficshaping.network.client.BurstyClient &
java -cp "/app/core-classes:/app/network-classes" com.trafficshaping.network.client.RandomClient &

echo "All components successfully launched!"
echo "System ready to process incoming traffic."

# Wait for all background jobs, preventing Docker container from silently terminating
wait -n
