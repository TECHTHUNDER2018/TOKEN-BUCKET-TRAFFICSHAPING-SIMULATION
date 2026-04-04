#!/bin/bash
echo "Starting Token Bucket Simulator Server Provisioning..."

echo "1/3 Updating Ubuntu package lists..."
sudo apt-get update -y

echo "2/3 Installing Docker and Docker Compose..."
sudo apt-get install -y docker.io docker-compose
sudo systemctl enable docker
sudo systemctl start docker

echo "3/3 Building application containers and starting daemon..."
sudo docker-compose up --build -d

echo "Deployment Successful!"
echo "Check your dashboard at http://<YOUR_SERVER_IP>:8080"
