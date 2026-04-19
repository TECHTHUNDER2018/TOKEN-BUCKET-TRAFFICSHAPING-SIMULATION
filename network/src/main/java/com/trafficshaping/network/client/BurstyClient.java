package com.trafficshaping.network.client;

import java.io.DataOutputStream;
import java.net.Socket;

public class BurstyClient extends TrafficClient {
    public BurstyClient(String host, int port) {
        super(host, port);
    }

    @Override
    public void run() {
        System.out.println("Starting Bursty Traffic Client...");
        while (!Thread.currentThread().isInterrupted()) {
            try (Socket socket = new Socket(host, port);
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
                System.out.println("BurstyClient connected!");
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println("Sleeping for 2 seconds...");
                    Thread.sleep(2000);
                    System.out.println("Sending burst of 50 packets!");
                    for (int i = 0; i < 50; i++) {
                        if (Thread.currentThread().isInterrupted()) break;
                        sendPacket(out, 100);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                if (!Thread.currentThread().isInterrupted()) {
                    System.out.println("BurstyClient: retrying in 5s... (" + e.getMessage() + ")");
                    try { Thread.sleep(5000); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        System.out.println("BurstyClient stopped.");
    }

    public static void main(String[] args) {
        new BurstyClient(getEnvHost(), getEnvPort()).run();
    }
}
