package org.distributed.kvstore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MockServer implements AutoCloseable {
    private final ServerSocket serverSocket;
    private String pingResponse = "PONG";

    public  MockServer() {
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
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                Thread handler = new Thread(() -> handle(socket));
                handler.setDaemon(true);
                handler.start();
            } catch (IOException e) {
                return;
            }
        }
    }

    private void handle(Socket socket) {
        try (socket;
             BufferedReader requestReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter responseWriter = new PrintWriter(socket.getOutputStream(), true)) {
            String request;
            while ((request = requestReader.readLine()) != null) {
                if (request.equals("PING")) {
                    responseWriter.println(pingResponse);
                }
            }
        } catch (IOException e) {
            // client went away
        }
    }

    private void cleanUp() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error while closing the mock server");
        }
    }

     void setIncorrectPingResponse() {
        pingResponse = "Blah";
    }

    @Override
    public void close() throws Exception {
        cleanUp();
    }
}
