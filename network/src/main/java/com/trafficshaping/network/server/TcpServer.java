package com.trafficshaping.network.server;

import com.trafficshaping.core.LeakyBucket;
import com.trafficshaping.core.Packet;
import com.trafficshaping.core.TokenBucket;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServer {
    private final int port;
    private final TokenBucket tokenBucket;
    private final LeakyBucket leakyBucket;
    private final ExecutorService clientPool;
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public TcpServer(int port, TokenBucket tokenBucket, LeakyBucket leakyBucket) {
        this.port = port;
        this.tokenBucket = tokenBucket;
        this.leakyBucket = leakyBucket;
        this.clientPool = Executors.newCachedThreadPool();
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        System.out.println("TCP Server started on port " + port);

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                clientPool.submit(() -> handleClient(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("Accept failed: " + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
            clientPool.shutdownNow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket) {
        try (DataInputStream in = new DataInputStream(socket.getInputStream())) {
            System.out.println("Client connected: " + socket.getRemoteSocketAddress());
            long packetsProcessed = 0;
            while (running) {
                String packetId = in.readUTF();
                int size = in.readInt();
                Packet packet = new Packet(packetId, size);
                tokenBucket.processPacket(packet);
                leakyBucket.processPacket(packet);
                packetsProcessed++;
            }
        } catch (IOException e) {
            System.out.println("Client disconnected or error: " + socket.getRemoteSocketAddress());
        }
    }
}
