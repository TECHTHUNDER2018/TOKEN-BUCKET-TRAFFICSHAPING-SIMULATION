package com.trafficshaping.network.client;

import java.io.DataOutputStream;
import java.net.Socket;

public class SteadyClient extends TrafficClient {
    public SteadyClient(String host, int port) {
        super(host, port);
    }

    @Override
    public void run() {
        System.out.println("Starting Steady Traffic Client...");
        try (Socket socket = new Socket(host, port);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            while (true) {
                sendPacket(out, 100);
                Thread.sleep(50); // Steady 20 packets per second
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new SteadyClient(getEnvHost(), getEnvPort()).run();
    }
}
