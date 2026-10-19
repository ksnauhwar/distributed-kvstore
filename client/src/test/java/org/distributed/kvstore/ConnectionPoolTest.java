package org.distributed.kvstore;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionPoolTest {
    MockServer server = new MockServer();

    @Test
    public void createsConnectionPool() throws MalformedURLException {
        ConnectionPool connectionPool = new ConnectionPool(URL.of(URI.create("http://localhost:" + server.port()),null));
        assertNotNull(connectionPool);
    }

    @Test
    public void doesNotCreateConnectionPool() {
        Assertions.assertThrows(RuntimeException.class, () -> new ConnectionPool(URL.of(URI.create("http://nonexistent:" + server.port()),null)));
    }

    @Test
    public void acquiresConnectionFromPool() throws MalformedURLException {
        ConnectionPool connectionPool = new ConnectionPool(URL.of(URI.create("http://localhost:" + server.port()),null));
        assertNotNull(connectionPool.acquire());
    }

    @Test
    public void releasesConnectionBackToPool() throws Exception {
        ConnectionPool connectionPool = new ConnectionPool(URL.of(URI.create("http://localhost:" + server.port()),null));
        Connection connection = connectionPool.acquire();
        assertTrue(connectionPool.release(connection));
    }
}
