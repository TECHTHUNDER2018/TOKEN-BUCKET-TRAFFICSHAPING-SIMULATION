#!/bin/bash
echo "Starting Token Bucket Traffic Shaping Simulator Super-Container..."

# Start the Spring Boot Web Server in the background
# Pass Render's dynamic $PORT so it binds correctly (defaults to 8080 locally)
echo "Booting Web Dashboard and TCP Socket Engine on port ${PORT:-8080}..."
java -Dserver.port=${PORT:-8080} -jar app.jar &
SERVER_PID=$!

# Wait for Spring Boot to fully initialize its ServerSocket context
echo "Waiting 15 seconds for server to initialize..."
sleep 15

# Verify the server actually started before launching clients
if ! kill -0 $SERVER_PID 2>/dev/null; then
  echo "ERROR: Spring Boot server failed to start. Exiting."
  exit 1
fi

echo "Routing Internal Traffic Generators to localhost:9000..."

export SERVER_HOST=localhost
export SERVER_PORT=9000

# Start clients in the background
java -cp "/app/core-classes:/app/network-classes" com.trafficshaping.network.client.SteadyClient &
java -cp "/app/core-classes:/app/network-classes" com.trafficshaping.network.client.BurstyClient &
java -cp "/app/core-classes:/app/network-classes" com.trafficshaping.network.client.RandomClient &

echo "All components launched! Dashboard running on port ${PORT:-8080}"

# Wait for the server process specifically — keep the container alive as long as it runs
wait $SERVER_PID
