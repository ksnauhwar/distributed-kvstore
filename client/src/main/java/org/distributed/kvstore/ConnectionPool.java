package org.distributed.kvstore;

import java.net.URL;
import java.util.concurrent.*;

public class ConnectionPool {
    private static final int TCP_CONNECTION_POOL_SIZE = 8;
    private final BlockingQueue<Connection> pool = new LinkedBlockingQueue<>(TCP_CONNECTION_POOL_SIZE);
    private final ScheduledExecutorService houseKeeper;
    private final URL loadBalancerUrl;

    public ConnectionPool(URL loadBalancerUrl) {
        create(loadBalancerUrl);
        this.loadBalancerUrl = loadBalancerUrl;
        houseKeeper = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable,"connection-pool-house-keeper");
            thread.setDaemon(true);
            return thread;
        });
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanUp));


        houseKeeper.scheduleAtFixedRate(this::replaceDeadConnections, 30L, 30L, TimeUnit.SECONDS);
    }

    private void replaceDeadConnections() {
        for (int index = 0; index < TCP_CONNECTION_POOL_SIZE; index++) {
            Connection connection = pool.poll();

            if (connection == null) {
                pool.offer(new Connection(this.loadBalancerUrl));
                continue;
            }

            if (connection.isDead()) {
                try {
                    connection.close();
                } catch (Exception e) {
                    System.out.println("Error closing connection");
                }
                pool.offer(new Connection(loadBalancerUrl));
            } else {
                pool.offer(connection);
            }
        }
    }



    private void create(URL loadBalancerUrl) {
        for (int index = 0; index < TCP_CONNECTION_POOL_SIZE; index++) {
            pool.offer(new Connection(loadBalancerUrl));
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
}
