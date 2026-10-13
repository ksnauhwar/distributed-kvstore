package org.distributed.kvstore;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionTest {

    final private MockServer server = new MockServer();

    @Test
    public void connectionIsAlive() throws Exception {
        try (Connection connection = new Connection(URL.of(URI.create("http://localhost:" + server.port()), null))) {
            assertFalse(connection.isDead());
        }
    }

    @Test
    public void connectionIsDead() throws Exception {
        Connection connection = new Connection(URL.of(URI.create("http://localhost:" + server.port()),null));
        connection.close();
        assertTrue(connection.isDead());
    }
}
