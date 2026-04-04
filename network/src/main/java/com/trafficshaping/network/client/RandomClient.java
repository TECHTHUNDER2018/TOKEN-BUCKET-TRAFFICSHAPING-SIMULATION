package com.trafficshaping.network.client;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Random;

public class RandomClient extends TrafficClient {
    public RandomClient(String host, int port) {
        super(host, port);
    }

    @Override
    public void run() {
        System.out.println("Starting Random Traffic Client...");
        Random random = new Random();
        while (true) {
            try (Socket socket = new Socket(host, port);
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
                System.out.println("RandomClient connected!");
                while (true) {
                    sendPacket(out, 100);
                    Thread.sleep(10 + random.nextInt(490));
                }
            } catch (Exception e) {
                System.out.println("RandomClient: retrying in 5s... (" + e.getMessage() + ")");
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
            }
        }
    }

    public static void main(String[] args) {
        new RandomClient(getEnvHost(), getEnvPort()).run();
    }
}
