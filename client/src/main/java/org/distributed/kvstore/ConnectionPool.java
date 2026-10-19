package org.distributed.kvstore;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ConnectionPool {
    private static final int TCP_CONNECTION_POOL_SIZE = 8;
    private final BlockingQueue<Connection> pool = new LinkedBlockingQueue<>(TCP_CONNECTION_POOL_SIZE);
    private final ScheduledExecutorService houseKeeper;
    private final URL loadBalancerUrl;
    private final List<Connection> usedConnections = new ArrayList<>(TCP_CONNECTION_POOL_SIZE);

    public ConnectionPool(URL loadBalancerUrl) {
        create(loadBalancerUrl);
        this.loadBalancerUrl = loadBalancerUrl;
        houseKeeper = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable,"connection-pool-house-keeper");
            thread.setDaemon(true);
            return thread;
        });
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanUp));


        houseKeeper.scheduleAtFixedRate(this::replaceDeadConnections, 30L, 2L, TimeUnit.MINUTES);
    }

    private void replaceDeadConnections() {
        for (int index = 0; index < (TCP_CONNECTION_POOL_SIZE - usedConnections.size()); index++) {
            Connection connection = pool.poll();

            if (connection == null) {
                if (!pool.offer(new Connection(this.loadBalancerUrl))) {
                    System.out.println("Could not add connection to connection pool");
                }
                continue;
            }
            try {
                if (connection.isDead()) {
                    connection.close();
                    if (!pool.offer(new Connection(loadBalancerUrl))) {
                        System.out.println("Unable to replace dead connection");
                    }
                } else {
                    if (!pool.offer(connection)) {
                        System.out.println("Unable to add healthy connection back to the pool");
                        connection.close();
                    }
                }
            } catch (Exception e) {
                System.out.println("Error closing connection");
            }
        }

        maybeFillConnectionPool();
    }

    private void maybeFillConnectionPool() {
        if (pool.size() < (TCP_CONNECTION_POOL_SIZE - usedConnections.size())) {
            for (int index = 0; index < (TCP_CONNECTION_POOL_SIZE - pool.size()); index++) {
                if (!pool.offer(new Connection(loadBalancerUrl))) {
                    System.out.println("Unable to add connection to the pool. The pool is now deficient");
                }
            }
        }
    }

    private void create(URL loadBalancerUrl) {
        for (int index = 0; index < TCP_CONNECTION_POOL_SIZE; index++) {
            if (!pool.offer(new Connection(loadBalancerUrl))) {
                System.out.println("Unable to add connection to pool during pool creation");
            }
        }
    }

    private void cleanUp() {
        for (int index = 0; index < TCP_CONNECTION_POOL_SIZE; index++) {
            try {
                Connection connection = pool.poll();
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                System.out.println("Error closing a connection");
            }
        }

        houseKeeper.close();
    }

    public Connection acquire() {
        Connection connection = pool.poll();
        while ((connection == null || connection.isDead()) && !pool.isEmpty()) {
            connection = pool.poll();
        }
        usedConnections.add(connection);
        return connection != null && connection.isDead() ? null : connection;
    }

    public boolean release(Connection connection) {
        boolean isConnectionAddedBackToPool = pool.offer(connection);
        if (!isConnectionAddedBackToPool) return false;

        usedConnections.remove(connection);
        return true;
    }
}
