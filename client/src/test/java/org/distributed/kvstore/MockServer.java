package org.distributed.kvstore;

import java.io.IOException;
import java.net.ServerSocket;

public class MockServer {
    private final ServerSocket serverSocket;

    public MockServer() {
        try {
            serverSocket = new ServerSocket(0,8);

            Thread thread = new Thread(this::start);
            thread.setDaemon(true);
            thread.start();

            Runtime.getRuntime().addShutdownHook(new Thread(this::cleanUp));
        } catch (IOException e) {
            throw new RuntimeException("Unable to create server");
        }
    }

    public int port() {
        return serverSocket.getLocalPort();
    }

    public void start() {
        try {
            serverSocket.accept();
        } catch (IOException e) {
            throw new RuntimeException("Server unable to accept connections");
        }
    }

    private void cleanUp() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error while closing the mock server");
        }
    }
}
