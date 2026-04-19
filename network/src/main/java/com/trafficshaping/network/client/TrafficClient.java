package com.trafficshaping.network.client;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class TrafficClient implements Runnable {
    protected final String host;
    protected final int port;

    public TrafficClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public abstract void run();

    protected void sendPacket(DataOutputStream out, int size) throws IOException {
        out.writeUTF(java.util.UUID.randomUUID().toString());
        out.writeInt(size);
        out.flush();
    }

    protected static String getEnvHost() {
        return System.getenv("SERVER_HOST") != null ? System.getenv("SERVER_HOST") : "localhost";
    }

    protected static int getEnvPort() {
        return System.getenv("SERVER_PORT") != null ? Integer.parseInt(System.getenv("SERVER_PORT")) : 9000;
    }
}
