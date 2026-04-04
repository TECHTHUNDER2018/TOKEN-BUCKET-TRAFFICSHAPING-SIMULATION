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
        try (Socket socket = new Socket(host, port);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            while (true) {
                sendPacket(out, 100);
                // Sleep for random between 10ms and 500ms
                Thread.sleep(10 + random.nextInt(490));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new RandomClient(getEnvHost(), getEnvPort()).run();
    }
}
