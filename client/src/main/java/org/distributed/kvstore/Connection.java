package org.distributed.kvstore;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;

class Connection implements AutoCloseable {
    private final Socket socket;

    Connection(URL loadBalancerUrl) {
        try {
            socket = new Socket(loadBalancerUrl.getHost(), loadBalancerUrl.getPort());
            socket.setKeepAlive(true);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Unable to connect to server: %s:%s ",loadBalancerUrl.getHost(), loadBalancerUrl.getPort()));
        }
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }

    boolean isDead() {
        return !socket.isConnected() || socket.isClosed();
    }
}
