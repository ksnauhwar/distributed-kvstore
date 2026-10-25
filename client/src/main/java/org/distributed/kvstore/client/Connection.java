package org.distributed.kvstore.client;

import java.io.*;
import java.net.Socket;
import java.net.URL;

public class Connection implements AutoCloseable {
    private final Socket socket;
    private final BufferedReader responseReader;
    private final PrintWriter requestWriter;

    Connection(URL loadBalancerUrl) {
        try {
            socket = new Socket(loadBalancerUrl.getHost(), loadBalancerUrl.getPort());
            socket.setKeepAlive(true);
            responseReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            requestWriter = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Unable to connect to server: %s:%s ",loadBalancerUrl.getHost(), loadBalancerUrl.getPort()));
        }
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }

    boolean isDead() {
        if (socket.isClosed()) return true;
        try {
            socket.setSoTimeout(3000);

            requestWriter.println("PING");

            String response = responseReader.readLine();
            socket.setSoTimeout(0);
            if (response == null) return true;
            if (response.equals("PONG")) return false;

        } catch (IOException e) {
            return true;
        }

        return true;
    }
}
