package org.distributed.kvstore.client;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionTest {

    @Test
    public void connectionIsAlive() throws Exception {
        try (MockServer server = new MockServer();
             Connection connection = new Connection(URL.of(URI.create("http://localhost:" + server.port()), null))) {
            assertFalse(connection.isDead());
        }
    }

    @Test
    public void connectionIsDeadForAClosedSocket() throws Exception {
        try (MockServer server = new MockServer();
        Connection connection = new Connection(URL.of(URI.create("http://localhost:" + server.port()),null))) {
            connection.close();
            assertTrue(connection.isDead());
        }
    }

    @Test
    public void connectionIsDeadForIncorrectPingResponse() throws Exception {
        try (MockServer server = new MockServer();
          Connection connection = new Connection(URL.of(URI.create("http://localhost:" + server.port()),null))) {
            server.setIncorrectPingResponse();
            assertTrue(connection.isDead());
        }
    }
}
